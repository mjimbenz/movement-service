package com.bank.movement.service;

import com.bank.movement.api.model.MovementRequest;
import com.bank.movement.model.MovementEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementService {
    Mono<MovementEntity> registerMovement(MovementRequest req);
    Flux<MovementEntity> findByProductId(String productId);
    Flux<MovementEntity> findByCustomerId(String customerId);
    Mono<MovementEntity> findById(String id);
    Mono<Void> delete(String id);
}
