package com.bank.movement.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.bank.events.MovementCompletedEvent;
import com.bank.movement.repository.MovementRepository;

@EnableKafka
@Service
@RequiredArgsConstructor
@Slf4j
public class MovementCompletedConsumer {

    private final MovementRepository repository;

    @KafkaListener(topics = "movement-completed", groupId = "movement-service")
    public void listen(MovementCompletedEvent event) {
        log.info("Received MovementCompletedEvent for movementId: {}",
                event.getMovementId());

        repository.findById(event.getMovementId())
                .flatMap(movement -> {
                    movement.setActive(true);
                    movement.setStatus("COMPLETED");
                    return repository.save(movement);
                }).subscribe();
    }
}