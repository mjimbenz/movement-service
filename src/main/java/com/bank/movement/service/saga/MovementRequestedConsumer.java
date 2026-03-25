package com.bank.movement.service.saga;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class MovementRequestedConsumer {
//
//    private final PassiveProductService service;
//    private final MovementResponsePublisher publisher;
//
//    @KafkaListener(
//            topics = "movement-requested",
//            groupId = "passive-product-service"
//    )
//    public void listen(MovementRequestedEvent event) {
//
//        service.applyMovement(event.getProductId(), event.getAmount(), event.getMovementType())
//                .doOnSuccess(v -> publisher.publishCompleted(event.getMovementId()))
//                .doOnError(err -> publisher.publishRejected(event.getMovementId(), err.getMessage()))
//                .subscribe();
//    }
//}
