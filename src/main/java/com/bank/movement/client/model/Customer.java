package com.bank.movement.client.model;

import lombok.Builder;

@Builder
public record Customer(
        String id,
        String type,
        String documentType,
        String documentNumber,
        String fullname,
        String email,
        String phone
) {
}
