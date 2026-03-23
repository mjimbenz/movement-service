package com.bank.movement.client;

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
public class CustomerWebClient extends BaseWebClient {


    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackCustomer")
    @Retry(name = "customerService")
    @TimeLimiter(name = "customerService")
    public Mono<Customer> getCustomer(String id) {
        return client.get().uri("/customer-service/{id}", id)
                .retrieve()
                .bodyToMono(Customer.class)
                .switchIfEmpty(Mono.error(new BusinessException("Customer not found with id: " + id)));
    }
}
