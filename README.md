# StoreMaster Microservices Architecture

Este repositorio contiene la arquitectura de servicios distribuidos para la plataforma de gestión empresarial StoreMaster. El sistema implementa un diseño de microservicios escalable y seguro utilizando Spring Boot 3.3, optimizado para entornos de alta disponibilidad.

## Arquitectura del Sistema
La solución se compone de dos módulos independientes que interactúan mediante protocolos REST y lógica de sincronización interna:

1.  **Products Service (Puerto 8080):** Responsable de la gestión del catálogo maestro de productos, la autenticación mediante JSON Web Tokens (JWT) y el procesamiento de transacciones comerciales.
2.  **Inventory Service (Puerto 8081):** Encargado de la gestión de niveles de stock físico, registros de movimientos de almacén y la actualización de estados de disponibilidad.

## Especificaciones Tecnológicas
*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 3.3
*   **Seguridad:** Spring Security con implementación de JWT
*   **Gestión de Base de Datos:** Flyway para migraciones controladas de esquemas
*   **Resiliencia:** Resilience4j para la implementación de patrones Circuit Breaker y Retry
*   **Persistencia:** PostgreSQL 18
*   **Containerización:** Docker para estandarización de entornos

## Instrucciones para Despliegue Local

### Requisitos Previos
*   Java Development Kit (JDK) 21 o superior
*   Apache Maven 3.9 o superior
*   Instancia de PostgreSQL activa

### Procedimiento de Instalación
1.  **Clonación del repositorio:**
    ```bash
    git clone https://github.com/YURIVIVIANAOCAMPO/products-service.git
    ```
2.  **Configuración de Base de Datos:**
    Crear una instancia de base de datos denominada `store_db`. Actualizar los archivos `application.properties` en cada módulo con las credenciales correspondientes.
3.  **Ejecución de Servicios:**
    ```bash
    # Ejecución del Servicio de Productos
    cd products-service && mvn spring-boot:run
    
    # Ejecución del Servicio de Inventario
    cd ../inventory-service && mvn spring-boot:run
    ```
4.  **Documentación de la API:**
    *   Products Service: `http://localhost:8080/swagger-ui.html`
    *   Inventory Service: `http://localhost:8081/swagger-ui.html`

## Documentación Técnica Avanzada
Para una comprensión profunda de las decisiones de diseño y el cumplimiento de los requisitos de la prueba técnica, consulte los siguientes documentos:
*   [Arquitectura del Sistema (Diagrama C4)](./docs/architecture_diagram.md)
*   [Decisiones Técnicas (Concurrencia, Resiliencia e Idempotencia)](./docs/technical_decisions.md)
*   [Evidencia de Ejecución de Pruebas](./docs/test_evidence.md)

## Ejecución con Docker Compose
El sistema puede desplegarse íntegramente (Base de Datos + Microservicios) mediante la orquestación de Docker:

```bash
docker-compose up --build
```
*Nota: Asegúrese de tener Docker Desktop instalado y en ejecución.*

## Ejemplos de Interacción (CURL)

### Autenticación (Obtención de JWT)
```bash
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin"}'
```

### Consultar Inventario (Uso de API Key interna)
```bash
curl -X GET http://localhost:8081/inventory/{productId} \
     -H "x-api-key: inventory-secret-key"
```

### Ejecutar Compra (Idempotencia con Idempotency-Key)
```bash
curl -X POST http://localhost:8080/products/{productId}/purchase \
     -H "Content-Type: application/json" \
     -H "Idempotency-Key: trx-uuid-001" \
     -d '{"quantity": 1}'
```

## Despliegue en Entornos de Producción (Render)
Se ha seleccionado la plataforma Render basándose en los siguientes criterios técnicos:
*   **Eficiencia de Costos:** Utilización de la capa gratuita para despliegues Dockerizados.
*   **Gestión de Recursos:** Optimización de memoria mediante `-Xmx384m` para operar con estabilidad en límites de 512 MB.
*   **Base de Datos:** PostgreSQL 18.3 Cloud.

## Configuración de Seguridad y CORS
El sistema autoriza peticiones cross-origin únicamente para:
*   `http://localhost:5173` (Desarrollo)
*   `https://*.vercel.app` (Producción en Vercel)

---
*StoreMaster Enterprise - Documentación Técnica Oficial.*

