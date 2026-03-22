# Movement Service

Movement3 designMovement Service is a training microservice responsible for
- Register movements (training only)
- Consult movements by customer or product
- Soft delete + audit
- Logging with MDC (traceId, customerId, productId)
- Resilience with Resilience4j
- Integration with Passive Product Service
- Integration with Active Product Service
- Integration with Customer Service

## Movement Types
- DEPOSIT
- WITHDRAWAL
- CREDIT_PAYMENT
- CARD_CONSUMPTION

## Architecture
Movement Service depends on:
- Customer Service
- Active Product Service
- Passive Product Service

It does NOT modify financial balances in this training context.

## Execution
registering and consulting movements associated with banking products,
both passive (accounts) and active (credits / credit cards).

## Features

- Reactive WebFlux architecture
- Resilience with Resilience4j
- Logging with MDC (traceId, customerId, productId)
- Soft delete with audit
- Integration with external services (Customer, Active Product, Passive Product)