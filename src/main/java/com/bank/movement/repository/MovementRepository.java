package com.bank.movement.repository;

import com.bank.movement.model.MovementEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface MovementRepository extends ReactiveMongoRepository<MovementEntity, String> {

    Flux<MovementEntity> findByCustomerIdAndActiveTrue(String id);
    Flux<MovementEntity> findByProductIdAndActiveTrue(String id);
    Mono<MovementEntity> findByIdAndActiveTrue(String id);

    Mono<Integer> countByProductIdAndCreatedAtBetween(
            String productId,
            LocalDateTime localDateTime,
            LocalDateTime localDateTime1);
}
