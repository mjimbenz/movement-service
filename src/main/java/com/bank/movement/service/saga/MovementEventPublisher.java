package com.bank.movement.service.saga;

import com.bank.events.MovementRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovementEventPublisher {

    private final KafkaTemplate<String, MovementRequestedEvent> kafkaTemplate;

    public void publish(MovementRequestedEvent event) {
        kafkaTemplate.send("movement-requested", event.getMovementId(), event);
    }
}