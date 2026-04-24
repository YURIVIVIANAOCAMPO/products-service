# Decisiones Técnicas y Arquitectura - StoreMaster

Este documento detalla las justificaciones técnicas y estrategias de implementación utilizadas para cumplir con los requisitos de la prueba técnica.

## 1. Estrategia de Concurrencia (Optimistic Locking)
Para evitar que dos compras simultáneas dejen el stock en negativo, se implementó **Optimistic Locking** mediante la anotación `@Version` de JPA en la entidad `Inventory`.
*   **Funcionamiento:** Cada registro tiene una versión. Si dos transacciones intentan actualizar el mismo registro, la segunda fallará con una `ObjectOptimisticLockingFailureException` al detectar que la versión ya cambió, garantizando la integridad de los datos sin bloquear la base de datos (Pessimistic Locking).

## 2. Idempotencia
El servicio de inventario garantiza la idempotencia en el endpoint `/purchases` mediante el encabezado `Idempotency-Key`.
*   **Mecanismo:** Se utiliza una tabla de base de datos para registrar cada llave procesada. Si llega una petición con una llave existente, el sistema retorna el resultado previo en lugar de procesar el descuento de nuevo.

## 3. Resiliencia y Tolerancia a Fallos
Se utiliza **Resilience4j** para proteger la comunicación entre microservicios:
*   **Circuit Breaker:** Si el servicio de Inventario cae, el servicio de Productos entra en "estado abierto" y retorna un valor por defecto (o error controlado) inmediatamente, evitando cascadas de fallos.
*   **Retry:** Se configuraron reintentos automáticos para fallos transitorios de red.
*   **Timeouts:** Configuración estricta de 5 segundos para evitar que hilos se queden bloqueados esperando servicios lentos.

## 4. Comunicación y Eventos
*   **Sincronización:** Cuando el inventario cambia, el servicio emite un log estructurado `InventoryChanged` con el `correlation-id` de la petición.
*   **Sincronización de Estado:** El servicio de Inventario notifica al de Productos mediante una llamada REST protegida para actualizar el estado `ACTIVE/INACTIVE` del producto basado en el stock real.

## 5. Observabilidad
*   **Correlation ID:** Todas las peticiones generan un ID único que viaja a través de los microservicios en los encabezados, permitiendo trazabilidad completa en los logs.
*   **Health Checks:** Disponibles en `/actuator/health` para monitoreo de Liveness y Readiness.

---
*Documentación generada para la Evaluación Técnica de StoreMaster.*
