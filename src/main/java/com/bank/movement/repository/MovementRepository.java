package com.bank.movement.repository;

import com.bank.movement.model.MovementEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementRepository extends ReactiveMongoRepository<MovementEntity, String> {

    Flux<MovementEntity> findByCustomerIdAndActiveTrue();
    Mono<MovementEntity> findByProductIdAndActiveTrue();
    Mono<MovementEntity> findByIdAndActiveTrue();
}
