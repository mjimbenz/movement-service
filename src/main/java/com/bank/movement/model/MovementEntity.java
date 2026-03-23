package com.bank.movement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementEntity {

    @Id
    private String id;

    private String customerId;
    private String productId;
    private boolean productType;
    private String movementType;
    private Double amount;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
