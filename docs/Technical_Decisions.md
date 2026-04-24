# Decisiones Técnicas

Este documento detalla las decisiones clave de arquitectura y diseño aplicadas durante el desarrollo de la prueba técnica.

## 1. Clean Architecture y Patrón Multicapa
El proyecto se estructuró dividiendo responsabilidades estrictas:
- **Controllers**: Solo manejan HTTP (Request/Response) y validaciones de entrada (`@Valid`).
- **Services**: Contienen toda la lógica de negocio (validar duplicados, lógica de compra).
- **Repositories**: Exclusivamente para persistencia (Spring Data JPA).
- **DTOs & Mappers**: Se implementó un mapeador estricto (`ProductMapper`) para aislar la capa de persistencia (`Entity`) del exterior. Nunca se exponen las entidades en las respuestas de la API.

## 2. Base de Datos (PostgreSQL)
- Se eligió **PostgreSQL** por ser un motor relacional robusto que asegura propiedades ACID (Atomicidad, Consistencia, Aislamiento y Durabilidad).
- Ideal para el manejo de inventarios e información transaccional donde la integridad de los datos es vital (ej. consistencia de precios).

## 3. Resiliencia (Plus Senior)
- Para la comunicación con el `InventoryService` (usando `RestClient`), se implementó la librería **Resilience4j**.
- Se configuró el patrón **Retry** (`@Retry`). En un ambiente de microservicios, las caídas momentáneas de red son comunes. Con esto aseguramos que si el servicio de inventario falla o sufre un timeout, el `products-service` reintentará la petición automáticamente 3 veces (esperando 2 segundos) antes de rendirse y lanzar una excepción al cliente.

## 4. Pruebas Automatizadas (Testing Strategy)
- **Unitarias:** Mockito se usó para asegurar que la lógica pura del `ProductService` funcione correctamente.
- **Integración:** En lugar de mocks o una BD en memoria H2, se implementó **Testcontainers**. Esto asegura que las pruebas corran contra una instancia de PostgreSQL real efímera, detectando posibles incompatibilidades de drivers o SQL específico de la BD.
- **Contract Tests:** Se incluyó un setup de **Pact** (`InventoryClientPactTest`) asumiendo el rol de Consumer. Esto nos asegura que el microservicio de inventarios nunca rompa el contrato esperado (JSON) cuando decidan actualizarlo.

## 5. Manejo Centralizado de Errores
- Se implementó un `@RestControllerAdvice` (`GlobalExceptionHandler`). 
- **Ventaja:** Evita repetir bloques `try/catch` en los controladores y estandariza los códigos de error HTTP de salida:
  - `404 Not Found`: Si el UUID de producto no existe.
  - `409 Conflict`: Si el SKU está duplicado.
  - `422 Unprocessable Entity`: Si los datos enviados fallan validación o no hay stock.
