package com.bank.movement.service.saga;

import com.bank.events.MovementRejectedEvent;
import com.bank.movement.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementRejectedConsumer {

    private final MovementRepository repository;

    @KafkaListener(topics = "movement-rejected", groupId = "movement-service")
    public void listen(MovementRejectedEvent event) {
        log.info("Received MovementRejectedEvent for movementId: {}, reason: {}",
                event.getMovementId(), event.getReason());
        repository.findById(event.getMovementId())
                .flatMap(movement -> {
                    movement.setActive(false);
                    movement.setStatus("REJECTED");
                    movement.setDeletedAt(LocalDateTime.now());
                    return repository.save(movement);
                }).subscribe();
    }
}
