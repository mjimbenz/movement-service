package com.bank.movement.client;

import com.bank.movement.client.model.Customer;
import com.bank.movement.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class BaseWebClient {

    protected final WebClient webClient;

    protected BaseWebClient(@Value("${app.server.gateway}") String gateway) {
        this.webClient = WebClient.builder()
                .baseUrl(gateway)
                .build();
    }

    protected <T> Mono<T> handleFallback(String id, Throwable t) {

        log.error("[Fallback] id={}, cause={}",
                id, t != null ? t.getMessage() : "Unknown");

        if (t instanceof java.util.concurrent.TimeoutException) {
            return Mono.error(new BusinessException("Service timeout while fetching id=" + id));
        }

        if (t instanceof org.springframework.web.reactive.function.client.WebClientRequestException) {
            return Mono.error(new BusinessException("Target service is unreachable for id=" + id));
        }

        if (t instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
            return Mono.error(new BusinessException("Target service returned error for id=" + id));
        }

        return Mono.error(new BusinessException("Service unavailable for id=" + id));
    }
}