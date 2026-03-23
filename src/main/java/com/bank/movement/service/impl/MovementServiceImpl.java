package com.bank.movement.service.impl;

import com.bank.movement.api.model.MovementRequest;
import com.bank.movement.api.model.MovementTypeEnum;
import com.bank.movement.client.ActiveProductWebClient;
import com.bank.movement.client.CustomerWebClient;
import com.bank.movement.client.PasiveProductWebClient;
import com.bank.movement.exception.BusinessException;
import com.bank.movement.model.MovementEntity;
import com.bank.movement.repository.MovementRepository;
import com.bank.movement.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementServiceImpl implements MovementService {

    private final MovementRepository repository;
    private final CustomerWebClient customerClient;
    private final ActiveProductWebClient activeProductWebClient;
    private final PasiveProductWebClient pasiveProductWebClient;


    @Override
    public Mono<MovementEntity> registerMovement(MovementRequest req) {
        log.info("[Movement] Registering movement {}", req);
        return validateCustomer(req.getCustomerId())
                .then(validateProduct(req.getProductId()))
                .flatMap(productType -> validateMovementType(productType, req.getMovementType()))
                .flatMap(productType -> saveMovement(req, productType));
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
    // 3. VALIDAR TIPO DE MOVIMIENTO SEGÚN PRODUCTO
    // -------------------------------------------------------------------------
    private Mono<String> validateMovementType(String productType, MovementTypeEnum movementType) {

        log.info("[Movement] Validating movementType={} for productType={}", movementType, productType);

        boolean valid = switch (productType) {
            case "PASSIVE" -> (movementType == MovementTypeEnum.DEPOSIT ||
                    movementType == MovementTypeEnum.WITHDRAWAL);
            case "ACTIVE" -> (movementType == MovementTypeEnum.CREDIT_PAYMENT ||
                    movementType == MovementTypeEnum.CARD_CONSUMPTION);
            default -> false;
        };

        if (!valid) {
            throw new BusinessException("Movement type " + movementType +
                    " does not apply to product type " + productType);
        }

        return Mono.just(productType);
    }



    // -------------------------------------------------------------------------
    // 4. GUARDAR EL MOVIMIENTO
    // -------------------------------------------------------------------------
    private Mono<MovementEntity> saveMovement(MovementRequest req, String productType) {

        MovementEntity movement = MovementEntity.builder()
                .customerId(req.getCustomerId())
                .productId(req.getProductId())
                .movementType(req.getMovementType().toString())
                .amount(req.getAmount().doubleValue())
                .productType(productType)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(movement)
                .doOnSuccess(m -> log.info("[Movement] Movement registered id={}", m.getId()));
    }





}