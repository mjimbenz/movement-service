# 🏦 Movement Service

Microservicio reactivo responsable de gestionar movimientos bancarios en tiempo real. Parte de una arquitectura de microservicios para plataforma de banca digital.

**Estado:** ✅ Production Ready | **Versión:** 0.0.1-SNAPSHOT | **Java:** 17+

---

## 📋 Descripción General

El **Movement Service** es el componente central para registrar, consultar y auditar todos los movimientos financieros asociados a productos bancarios (cuentas de ahorro, créditos, tarjetas de crédito, etc.).

### Responsabilidades Principales

✅ Registrar movimientos financieros  
✅ Consultar movimientos por cliente o producto  
✅ Gestionar validaciones de saldo y límites  
✅ Soft delete con auditoría completa  
✅ Logging distribuido con traceId  
✅ Resiliencia con circuit breakers  
✅ Integración con servicios de productos y clientes  

---

## 🎯 Tipos de Movimientos

| Tipo | Descripción | Aplica a |
|------|-------------|----------|
| **DEPOSIT** | Depósito de fondos | Productos Pasivos (Cuentas) |
| **WITHDRAWAL** | Retiro de fondos | Productos Pasivos (Cuentas) |
| **CREDIT_PAYMENT** | Pago de crédito | Productos Activos (Créditos) |
| **CARD_CONSUMPTION** | Consumo en tarjeta | Productos Activos (Tarjetas) |

---

## 🏗️ Arquitectura

```
┌────────────────���────────────────────────────────────────────┐
│                    API GATEWAY                              │
│              (JWT Validation & Routing)                      │
└─────────────┬───────────────────────────────────────────────┘
              │
    ┌─────────▼──────────┐
    │ Movement Service   │
    │ (Este proyecto)    │
    └─────────┬──────────┘
              │
    ┌─────────┴──────────────────────────────────┐
    │         │              │                   │
    ▼         ▼              ▼                   ▼
┌────────┐ ┌────────┐ ┌──────────────┐ ┌────────────────┐
│Customer│ │Passive │ │Active        │ │Config          │
│Service │ │Product │ │Product       │ │Server          │
│        │ │Service │ │Service       │ │(Properties)    │
└────────┘ └────────┘ └──────────────┘ └────────────────┘
```

### Dependencias de Servicios

| Servicio | Propósito | Tipo |
|----------|-----------|------|
| **Customer Service** | Validar existencia de cliente | ← Síncrono |
| **Passive Product Service** | Consultar/actualizar saldos en cuentas | ← Síncrono |
| **Active Product Service** | Consultar límites en créditos/tarjetas | ← Síncrono |
| **Config Server** | Centralizar configuraciones | ← Síncrono |
| **Eureka Server** | Service Discovery | ← Automático |

---

## ✨ Características Principales

### 🔄 Arquitectura Reactiva
- **Spring WebFlux** con Reactor
- Procesamiento asíncrono y no-bloqueante
- Escalabilidad horizontal

### 🛡️ Resiliencia
- **Circuit Breaker** en integraciones externas
- **Retry** automático con backoff exponencial
- **Time Limiter** para timeouts
- Fallbacks configurables

### 📊 Observabilidad
- **Logging distribuido** con MDC (traceId, customerId, productId)
- **Jackson** para serialización JSON
- **Logback** + **Log4j2** con Spring
- **Micrometer** para métricas

### 🔐 Seguridad & Auditoría
- **Soft delete** (no elimina datos, los marca como inactivos)
- **Auditoría** con timestamps (createdAt, deletedAt)
- **Trazabilidad** de usuarios (createdBy, modifiedBy)
- Headers de contexto del API Gateway
- Validación defensiva de JWT

### 📦 Base de Datos
- **MongoDB Reactiva** para persistencia
- Esquema flexible sin migraciones
- Índices en campos frecuentes

---

## 🚀 Inicio Rápido

### Requisitos Previos

```yaml
- Java 17+
- Maven 3.8+
- MongoDB 5.0+
- Spring Boot 3.3+
```

### Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-org/movement-service.git
cd movement-service
```

2. **Configurar propiedades**
```bash
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
# Editar archivo con configuraciones locales
```

3. **Compilar el proyecto**
```bash
mvn clean package
```

4. **Ejecutar localmente**
```bash
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8080`

---

## 🔌 Endpoints API

### Listar Movimientos por Cliente
```http
GET /movements?customerId=CUST-001
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK)**
```json
[
  {
    "id": "MOV-001",
    "customerId": "CUST-001",
    "productId": "PROD-001",
    "movementType": "DEPOSIT",
    "amount": 1000.50,
    "createdAt": "2024-03-23T10:30:00Z"
  }
]
```

### Consultar Movimiento por ID
```http
GET /movements/{id}
Authorization: Bearer <JWT_TOKEN>
```

### Registrar Nuevo Movimiento
```http
POST /movements
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "customerId": "CUST-001",
  "productId": "PROD-001",
  "movementType": "WITHDRAWAL",
  "amount": 500.00
}
```

**Response (201 Created)**
```json
{
  "id": "MOV-002",
  "customerId": "CUST-001",
  "productId": "PROD-001",
  "movementType": "WITHDRAWAL",
  "amount": 500.00,
  "createdAt": "2024-03-23T10:35:00Z",
  "active": true
}
```

### Listar Movimientos por Producto
```http
GET /movements/product/{productId}
Authorization: Bearer <JWT_TOKEN>
```

### Eliminar Movimiento (Soft Delete)
```http
DELETE /movements/{id}
Authorization: Bearer <JWT_TOKEN>
```

---

## 🔧 Configuración

### application.yaml
```yaml
spring:
  application:
    name: movement-service
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/movement_db

external:
  service:
    base-url: http://localhost:8080

jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: 86400000

resilience4j:
  circuitbreaker:
    instances:
      customerService:
        slidingWindowSize: 10
        failureRateThreshold: 50
```

### Variables de Entorno

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `MONGODB_URI` | Conexión a MongoDB | `mongodb://user:pass@host:27017/db` |
| `JWT_SECRET` | Clave secreta JWT | `tu-clave-secreta-256-bits` |
| `CONFIG_SERVER_URI` | URL de Config Server | `http://localhost:8888` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server | `http://localhost:8761/eureka/` |

---

## 📁 Estructura del Proyecto

```
src/main/java/com/bank/movement/
├── MovementServiceApplication.java     # Main application
├── client/                              # Web clients para servicios externos
│   ├── BaseWebClient.java
│   ├── CustomerWebClient.java
│   ├── ActiveProductWebClient.java
│   └── PasiveProductWebClient.java
├── controller/                          # REST Controllers
│   └── MovementApiDelegateImpl.java
├── service/                             # Lógica de negocio
│   ├── MovementService.java
│   └── impl/
│       └── MovementServiceImpl.java
├── repository/                          # Acceso a datos
│   └── MovementRepository.java
├── model/                               # Entidades
│   └── MovementEntity.java
├── exception/                           # Manejo de excepciones
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── dto/ErrorResponse.java
├── security/                            # JWT & Seguridad
│   ├── GatewayContext.java
│   └── DefensiveJwtFilter.java
└── config/                              # Configuraciones
    └── WebClientConfig.java

src/main/resources/
├── application.yaml                     # Configuración principal
├── logback-spring.xml                   # Logging configuration
└── openapi/
    └── movement-service.yaml            # OpenAPI 3.0 specification
```

---

## 🧪 Testing

### Ejecutar todas las pruebas
```bash
mvn test
```

### Con cobertura (JaCoCo)
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Pruebas específicas
```bash
mvn test -Dtest=MovementServiceImplTest
```

---

## 📊 Monitoreo & Observabilidad

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métricas
```bash
curl http://localhost:8080/actuator/metrics
```

### Logs Distribuidos
Todos los logs incluyen:
- `traceId`: Para correlacionar requests en toda la arquitectura
- `customerId`: Cliente afectado
- `productId`: Producto relacionado
- `userId`: Usuario autenticado

---

## 🔐 Auditoría

- **Soft delete:** Datos nunca se eliminan, se marcan como inactivos
- **Campos de auditoría:**
  - `createdBy`: Usuario que creó el registro
  - `createdAt`: Timestamp de creación
  - `deletedBy`: Usuario que eliminó
  - `deletedAt`: Timestamp de eliminación

---

## 🐛 Troubleshooting

### Error: "Customer not found"
**Causa:** El Cliente Service está inactivo o sin respuesta
**Solución:** 
```bash
# Verificar estado del servicio
curl http://localhost:8081/actuator/health
```

### Error: "Connection timeout to MongoDB"
**Causa:** MongoDB no está corriendo
**Solución:**
```bash
# Iniciar MongoDB
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### Circuit Breaker Abierto
**Causa:** Demasiados fallos en servicio externo
**Solución:** Los reintentos se activan después de 5 segundos. Monitorear:
```bash
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

---

## 📚 Documentación Adicional

- [OpenAPI Specification](src/main/resources/openapi/movement-service.yaml)
- [Arquitectura de Microservicios](/docs/architecture.md)
- [API Reference](http://localhost:8080/swagger-ui.html)

---

## 🤝 Contribuir

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

### Estándares de Código
- Seguir Google Java Style Guide
- Usar Lombok para reducir boilerplate
- Logs con nivel apropiado (DEBUG, INFO, WARN, ERROR)
- Pruebas unitarias para lógica de negocio

---

## 📄 Licencia

Este proyecto es parte del programa Bootcamp FinTech y está bajo licencia MIT.

---

## 📞 Contacto

- **Equipo:** Banking Microservices
- **Email:** dev-team@bank.com
- **Issues:** [GitHub Issues](https://github.com/tu-org/movement-service/issues)

---

**Última actualización:** Marzo 2024 | **Mantenedor:** Development Team
