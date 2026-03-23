package com.bank.movement.client.model;

import lombok.Builder;

@Builder
public record BalanceUpdateRequest(
        double amount
) {
}
