# Decisiones Técnicas - Products Service

Este documento detalla las decisiones arquitectónicas y tecnológicas tomadas para el desarrollo del microservicio de Productos, cumpliendo con los estándares de nivel Senior de la prueba técnica.

## 1. Persistencia: PostgreSQL + Flyway
Se eligió **PostgreSQL** como base de datos relacional debido a la naturaleza de los datos (productos con relaciones claras) y la necesidad de integridad transaccional.
- **Flyway**: Se utiliza para el control de versiones del esquema de base de datos. Esto garantiza que cualquier cambio en el modelo (como la creación de la tabla de usuarios o productos) sea determinista y reproducible en cualquier entorno (Docker, CI/CD, etc.).
- **Validación de Hibernate**: Se configuró `spring.jpa.hibernate.ddl-auto=validate` para asegurar que el código Java coincida exactamente con lo definido en las migraciones de Flyway.

## 2. Seguridad: JWT + BCrypt + API Key
- **Autenticación**: Se implementó un flujo de **JWT (JSON Web Tokens)** para la comunicación con el frontend. Las contraseñas se almacenan cifradas usando **BCrypt**.
- **Comunicación Inter-Servicios**: Para asegurar la comunicación con el `Inventory Service`, se utiliza un header `x-api-key`. El `InventoryClient` inyecta automáticamente este header en cada petición.
- **Rate Limiting**: Se implementó un filtro de **Rate Limit** usando la librería `Bucket4j`. Actualmente está configurado para permitir un máximo de 20 peticiones por minuto por dirección IP, protegiendo al servicio de ataques de fuerza bruta o abuso.

## 3. Resiliencia: Resilience4j
La comunicación con el servicio de Inventario es crítica. Para evitar que fallos en cascada afecten al servicio de productos, se implementó:
- **Timeouts**: Configurados a nivel de cliente HTTP para no dejar hilos bloqueados indefinidamente.
- **Circuit Breaker**: Si el servicio de inventario empieza a fallar repetidamente, el circuito se abre y se ejecuta un método de *fallback* que devuelve un estado de stock "seguro" (por ejemplo, 0) junto con un log de error, manteniendo la disponibilidad del sistema.
- **Retries**: Se configuraron reintentos automáticos para fallos transitorios de red.

## 4. Observabilidad
- **MDC (Mapped Diagnostic Context)**: Se utiliza un filtro web para capturar o generar un `X-Correlation-ID`. Este ID se propaga en todos los logs de la petición y se devuelve en el header de la respuesta, permitiendo la trazabilidad completa (*Distributed Tracing*).
- **Log Estructurado**: Los logs siguen un patrón consistente que incluye el ID de correlación.
- **Spring Boot Actuator**: Expone endpoints de `/health` y `/metrics` para monitoreo.

## 5. Estándar JSON:API
Todas las respuestas (éxito y error) cumplen con el estándar **JSON:API**.
- Los éxitos están envueltos en un objeto `{"data": ...}`.
- Los errores devuelven un array `{"errors": [...]}` con códigos de estado HTTP semánticos (404, 409, 422, 500).

## 6. Concurrencia y Consistencia
Para cumplir con el requerimiento de que dos compras simultáneas no dejen el stock en negativo:
- **Locking Optimista**: Se ha planificado el uso de la anotación `@Version` de JPA en la entidad de Inventario. Esto asegura que si dos transacciones intentan actualizar el mismo registro al mismo tiempo, una de ellas fallará con una `ObjectOptimisticLockingFailureException`, la cual será capturada para informar al usuario, evitando inconsistencias de stock.

## 7. Idempotencia
- **Idempotency-Key**: Siguiendo las reglas de la prueba, los endpoints críticos (como la compra en el `Inventory Service`) requieren un header `Idempotency-Key`. El servicio validará este ID contra una caché o tabla de auditoría para asegurar que una petición reintentada no procese el descuento de stock dos veces.

