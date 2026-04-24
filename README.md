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

## Despliegue en Entornos de Producción (Render)

Se ha seleccionado la plataforma Render para el alojamiento del backend basándose en los siguientes criterios técnicos:
*   **Eficiencia de Costos:** Utilización de la capa gratuita para despliegues Dockerizados sin inversión inicial.
*   **Gestión de Recursos:** Optimización del uso de memoria mediante la configuración del flag `-Xmx384m` en los archivos Dockerfile, garantizando estabilidad dentro de los límites de 512 MB de la instancia.
*   **Base de Datos Gestionada:** Implementación de PostgreSQL 18.3 en infraestructura Cloud.

## Configuración de Seguridad y CORS
El sistema restringe las peticiones cross-origin únicamente a los siguientes dominios autorizados:
*   `http://localhost:5173` (Entorno de Desarrollo)
*   `https://*.vercel.app` (Entorno de Producción)

---
*StoreMaster Enterprise - Documentación Técnica Oficial.*
