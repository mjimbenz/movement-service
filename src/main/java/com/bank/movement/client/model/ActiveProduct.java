package com.bank.movement.client.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ActiveProduct(
         String id,
         String customerId,
         String productType,
         Double creditLimit,
         Double balance,

         boolean active,

         LocalDateTime createdAt,
         LocalDateTime updatedAt,
         LocalDateTime deletedAt
) {
}
