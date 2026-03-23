package com.bank.movement.client;

import com.bank.movement.client.model.ActiveProduct;
import com.bank.movement.client.model.Customer;
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
public class ActiveProductWebClient extends BaseWebClient{

    @CircuitBreaker(name = "active-product-service", fallbackMethod = "fallbackCustomer")
    @Retry(name = "active-product-service")
    @TimeLimiter(name = "active-product-service")
    public Mono<ActiveProduct> getProduct(String id) {
        return client.get().uri("/active-product-service/{id}", id)
                .retrieve()
                .bodyToMono(ActiveProduct.class)
                .switchIfEmpty(Mono.error(new BusinessException("Product not found with id: " + id)));
    }
}
