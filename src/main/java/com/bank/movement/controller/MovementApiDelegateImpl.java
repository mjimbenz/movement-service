package com.bank.movement.controller;

import com.bank.movement.api.MovementApiDelegate;
import com.bank.movement.api.model.Movement;
import com.bank.movement.api.model.MovementRequest;
import com.bank.movement.api.model.MovementTypeEnum;
import com.bank.movement.model.MovementEntity;
import com.bank.movement.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovementApiDelegateImpl implements MovementApiDelegate {

    private final MovementService movementService;

    @Override
    public Mono<ResponseEntity<Flux<Movement>>> rootGet(String customerId, ServerWebExchange exchange) {
        log.info("[api] Getting movements for customerId={}", customerId);
        return Mono.just(ResponseEntity.ok(movementService.findByCustomerId(customerId).map(this::toModel)))
                .doOnSuccess(c -> log.info("[api] Successfully retrieved movements for customerId={}", customerId))
                .onErrorResume(err -> {
                    log.error("[api] Error retrieving movements for customerId={}, error={}", customerId, err.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @Override
    public Mono<ResponseEntity<Movement>> rootPost(Mono<MovementRequest> movementRequest, ServerWebExchange exchange) {
        log.info("[api] Creating movement -> {}", movementRequest);

        return movementRequest.map(this::toEntity)
                .flatMap(movementService::registerMovement)
                .doOnSuccess(m -> log.info("[api] Successfully created movement for customerId={}", m.getCustomerId()))
                .doOnError(err -> log.error("[api] Error creating movement, error={}", err.getMessage()))
                .map(this::toModel)
                .map(ResponseEntity::ok);


    }

    @Override
    public Mono<ResponseEntity<Movement>> idGet(String id, ServerWebExchange exchange) {
        log.info("[api] Getting movement by id={}", id);
        return movementService.findById(id)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .doOnSuccess(c -> log.info("[api] Successfully retrieved movement with id={}", id))
                .onErrorResume(err -> {
                    log.error("[api] Error retrieving movement with id={}, error={}", id, err.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<Movement>>> productProductIdGet(String productId, String customerId, ServerWebExchange exchange) {
        log.info("[api] Getting movements for productId={} and customerId={}", productId, customerId);
        return Mono.just(ResponseEntity.ok(movementService.findByProductId(productId).map(this::toModel)))
                .doOnSuccess(c -> log.info("[api] Successfully retrieved movements for productId={} and customerId={}", productId, customerId))
                .onErrorResume(err -> {
                    log.error("[api] Error retrieving movements for productId={} and customerId={}, error={}", productId, customerId, err.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> idDelete(String id, ServerWebExchange exchange) {
        return movementService.delete(id)
                .doOnSuccess(v -> log.info("[api] Successfully deleted movement with id={}", id))
                .doOnError(err -> log.error("[api] Error deleting movement with id={}, error={}", id, err.getMessage()))
                .thenReturn(ResponseEntity.noContent().build());
    }



    private MovementEntity toEntity(MovementRequest movementRequest) {
        return  MovementEntity.builder()
                .customerId(movementRequest.getCustomerId())
                .productId(movementRequest.getProductId())
                .movementType(movementRequest.getMovementType().getValue())
                .amount(movementRequest.getAmount().doubleValue())
                .build();
    }


    private Movement toModel(MovementEntity entity) {
        return new Movement()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .productId(entity.getProductId())
                .movementType(MovementTypeEnum.fromValue(entity.getMovementType()))
                .amount(BigDecimal.valueOf(entity.getAmount()));
    }
}
