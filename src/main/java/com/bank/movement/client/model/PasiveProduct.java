package com.bank.movement.client.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PasiveProduct(
         String id,
         String customerId,
         String accountType,
         Double balance,
         Integer transactionLimit,
         Double maintenanceFee,
         Integer allowedMovementDay,


        // Soft delete + auditoría
         boolean active,
         LocalDateTime createdAt,
         LocalDateTime updatedAt,
         LocalDateTime deletedAt
) {
}
