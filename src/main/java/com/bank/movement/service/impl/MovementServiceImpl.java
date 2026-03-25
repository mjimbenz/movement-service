package com.bank.movement.service.impl;

import com.bank.events.MovementRequestedEvent;
import com.bank.movement.api.model.MovementTypeEnum;
import com.bank.movement.client.ActiveProductWebClient;
import com.bank.movement.client.CustomerWebClient;
import com.bank.movement.client.PasiveProductWebClient;
import com.bank.movement.exception.BusinessException;
import com.bank.movement.model.MovementEntity;
import com.bank.movement.repository.MovementRepository;
import com.bank.movement.service.MovementService;
import com.bank.movement.service.saga.MovementEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementServiceImpl implements MovementService {

    private final MovementRepository repository;
    private final CustomerWebClient customerClient;
    private final ActiveProductWebClient activeProductWebClient;
    private final PasiveProductWebClient pasiveProductWebClient;
    private final MovementEventPublisher publisher;




    @Override
    public Mono<MovementEntity> registerMovement(MovementEntity req) {
        log.info("[Movement] Registering movement {}", req);

        return validateCustomer(req.getCustomerId())
                .then(validateProduct(req.getProductId()))
                .flatMap(productType ->
                        validateMovementType(
                                productType,
                                MovementTypeEnum.fromValue(req.getMovementType()),
                                req.getProductId(),
                                req.getAmount()
                        )
                )
                .then(Mono.defer(() -> {

                    // Preparar movimiento para guardar
                    req.setActive(true);
                    req.setStatus("PENDING");
                    req.setCreatedAt(LocalDateTime.now());

                    return repository.save(req)
                            .doOnSuccess(m ->
                                    log.info("[Movement] Movement saved id={} customerId={}",
                                            m.getId(), m.getCustomerId())
                            );
                }))
                .flatMap(savedMovement -> {
                    // Construir evento Avro
                    MovementRequestedEvent event = MovementRequestedEvent.newBuilder()
                            .setMovementId(savedMovement.getId())
                            .setCustomerId(savedMovement.getCustomerId())
                            .setProductId(savedMovement.getProductId())
                            .setMovementType(savedMovement.getMovementType())
                            .setAmount(savedMovement.getAmount())
                            .setEventTimestamp(System.currentTimeMillis())
                            .build();

                    // Publicar evento
                    publisher.publish(event);
                    log.info("[Movement] Published MovementRequestedEvent for movementId={}", savedMovement.getId());

                    return Mono.just(savedMovement);
                });
    }

    @Override
    public Flux<MovementEntity> findByProductId(String productId) {

        log.info("[Service] Listing movements by productId={}", productId);

        return validateProduct(productId)
                .thenMany(repository.findByProductIdAndActiveTrue(productId))
                .doOnNext(m -> log.info("[Service] Movement found id={} type={} amount={}",
                        m.getId(), m.getMovementType(), m.getAmount()))
                .doOnComplete(() -> log.info("[Service] Completed listing movements for productId={}", productId))
                .doOnError(err -> log.error("[Service] Error listing movements for productId={} err={}",
                        productId, err.getMessage()));
    }



    @Override

    public Flux<MovementEntity> findByCustomerId(String customerId) {

        log.info("[Service] Listing movements by customerId={}", customerId);

        return validateCustomer(customerId)
                .thenMany(repository.findByCustomerIdAndActiveTrue(customerId))
                .doOnNext(m -> log.info("[Service] Movement found id={} productId={} type={}",
                        m.getId(), m.getProductId(), m.getMovementType()))
                .doOnComplete(() -> log.info("[Service] Completed listing movements for customerId={}", customerId))
                .doOnError(err -> log.error("[Service] Error listing movements for customerId={} err={}",
                        customerId, err.getMessage()));
    }



    @Override

    public Mono<MovementEntity> findById(String id) {

        log.info("[Service] Fetching movement by id={}", id);

        return repository.findByIdAndActiveTrue(id)
                .switchIfEmpty(Mono.error(new BusinessException("Movement not found: " + id)))
                .doOnSuccess(movement -> log.info("[Service] Movement found id={}", id))
                .doOnError(err -> log.error("[Service] Error fetching movement id={} err={}", id, err.getMessage()));
    }



    @Override

    public Mono<Void> delete(String id) {
        log.info("[Service] Soft deleting movement id={}", id);

        return repository.findByIdAndActiveTrue(id)
                .switchIfEmpty(Mono.error(new BusinessException("Movement not found: " + id)))
                .flatMap(movement -> {
                    movement.setActive(false);
                    movement.setDeletedAt(LocalDateTime.now());
                    log.info("[Service] Marked movement as deleted id={}", id);
                    return repository.save(movement);
                })
                .doOnSuccess(x -> log.info("[Service] Movement soft-deleted successfully id={}", id))
                .doOnError(err -> log.error("[Service] Error deleting movement id={} err={}", id, err.getMessage()))
                .then();
    }






    // -------------------------------------------------------------------------
    // 1. VALIDAR CLIENTE
    // -------------------------------------------------------------------------
    private Mono<Void> validateCustomer(String customerId) {
        return customerClient.getCustomer(customerId)
                .doOnSuccess(c -> log.info("[Movement] Customer validated {}", customerId))
                .switchIfEmpty(Mono.error(new BusinessException("Customer not found")))
                .then();
    }


    // -------------------------------------------------------------------------
    // 2. VALIDAR PRODUCTO (Active o Passive)
    // -------------------------------------------------------------------------
    private Mono<String> validateProduct(String productId) {

        log.info("[Movement] Validating product {}", productId);

        return activeProductWebClient.getProduct(productId)
                .doOnSuccess(p -> log.info("[Movement] Active product found {}", productId))
                .map(p -> "ACTIVE")

                .onErrorResume(activeErr -> {
                    log.warn("[Movement] Active product not found, trying passive {}", productId);

                    return pasiveProductWebClient.getProduct(productId)
                            .doOnSuccess(pp -> log.info("[Movement] Passive product found {}", productId))
                            .map(pp -> "PASSIVE")
                            .switchIfEmpty(Mono.error(new BusinessException("Product not found")));
                });
    }


    // -------------------------------------------------------------------------
// 3. VALIDAR TIPO DE MOVIMIENTO Y SALDO MÍNIMO
// -------------------------------------------------------------------------
    private Mono<String> validateMovementType(
            String productType,
            MovementTypeEnum movementType,
            String productId,
            Double amount
    ) {

        log.info("[Movement] Validating movementType={} for productType={}",
                movementType, productType);

        boolean valid = switch (productType) {
            case "PASSIVE" -> (movementType == MovementTypeEnum.DEPOSIT ||
                    movementType == MovementTypeEnum.WITHDRAWAL);
            case "ACTIVE" -> (movementType == MovementTypeEnum.CREDIT_PAYMENT ||
                    movementType == MovementTypeEnum.CARD_CONSUMPTION);
            default -> false;
        };

        if (!valid) {
            log.warn("[Movement] Movement {} NOT allowed for productType={}", movementType, productType);
            throw new BusinessException("Movement type " + movementType +
                    " does not apply to product type " + productType);
        }

        // SOLO PASSIVE y SOLO WITHDRAWAL
        if (productType.equals("PASSIVE")) {

            switch (movementType){
                case WITHDRAWAL -> {
                    log.info("[Movement] Checking balance for PASSIVE withdrawal | productId={} amount={}",
                            productId, amount);

                    return pasiveProductWebClient.getProduct(productId)
                            .flatMap(product -> {
                                double balance = product.balance();
                                if (balance < amount) {
                                    log.error("[Movement] Insufficient balance | balance={} amount={}", balance, amount);
                                    return Mono.error(new BusinessException("Insufficient balance for withdrawal"));
                                }
                                log.info("[Movement] Balance OK | balance={} amount={}", balance, amount);
                                return Mono.just("product");
                            });
                }
                case DEPOSIT -> {
                    log.info("[Movement] Deposit does not require balance check for PASSIVE | productId={} amount={}",
                            productId, amount);
                    return Mono.just("product");
                }
            }


        }
        return Mono.just(productType);
    }




    private Mono<Integer> countMovementsToday(String productId) {
        LocalDate today = LocalDate.now();

        return repository.countByProductIdAndCreatedAtBetween(
                productId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
    }











}