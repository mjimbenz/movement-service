package com.bank.movement.client;

import com.bank.movement.client.model.ActiveProduct;
import com.bank.movement.client.model.BalanceUpdateRequest;
import com.bank.movement.client.model.PasiveProduct;
import com.bank.movement.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class PasiveProductWebClient extends BaseWebClient{

    @CircuitBreaker(name = "passive-product-service", fallbackMethod = "fallbackCustomer")
    @Retry(name = "passive-product-service")
    @TimeLimiter(name = "passive-product-service")
    public Mono<PasiveProduct> getProduct(String id) {
        return client.get().uri("/passive-product-service/{id}", id)
                .retrieve()
                .bodyToMono(PasiveProduct.class)
                .switchIfEmpty(Mono.error(new BusinessException("Product not found with id: " + id)));
    }
    @CircuitBreaker(name = "passive-product-service", fallbackMethod = "fallbackCustomer")
    @Retry(name = "passive-product-service")
    @TimeLimiter(name = "passive-product-service")
    public Mono<PasiveProduct> updateBalance(String id, double amount) {
        return client.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/passive-product-service/{id}/balance")
                        .build(id)
                )
                .bodyValue(BalanceUpdateRequest.builder().amount(amount).build())
                .retrieve()
                .bodyToMono(PasiveProduct.class)
                .switchIfEmpty(Mono.error(new BusinessException("Product not found with id: " + id)));
    }
}
