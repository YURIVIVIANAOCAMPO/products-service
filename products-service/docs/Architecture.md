# Arquitectura del Sistema

Este diagrama muestra la arquitectura de los microservicios y sus interacciones.

```mermaid
C4Context
    title Diagrama de Arquitectura - Tienda Microservicios

    Person(customer, "Cliente/Frontend", "Usuario final que navega por los productos.")
    
    System_Boundary(b1, "Backend Ecosystem") {
        System(products_service, "Products Service", "Gestiona el catálogo de productos y usuarios.", "Java Spring Boot")
        System(inventory_service, "Inventory Service", "Gestiona el stock físico y las compras.", "Java Spring Boot")
        
        ContainerDb(products_db, "Products DB", "PostgreSQL", "Almacena productos y usuarios.")
        ContainerDb(inventory_db, "Inventory DB", "PostgreSQL", "Almacena stock y transacciones.")
    }

    Rel(customer, products_service, "Usa (JWT Auth)", "HTTPS/JSON:API")
    Rel(products_service, products_db, "Lee/Escribe", "JDBC/JPA")
    
    Rel(products_service, inventory_service, "Consulta Stock / Compra", "HTTPS/API Key (Resilience4j)")
    Rel(inventory_service, inventory_db, "Lee/Escribe (Optimistic Locking)", "JDBC/JPA")
    
    Rel(inventory_service, products_service, "Valida existencia de producto", "HTTPS")
```

## Flujos Principales
1. **Autenticación**: El cliente obtiene un JWT mediante `/auth/login`.
2. **Listado**: El cliente solicita `/products`. El servicio consulta su BD y opcionalmente el stock al `Inventory Service`.
3. **Resiliencia**: Si `Inventory Service` está caído, `Products Service` abre su **Circuit Breaker** y responde con un stock degradado sin interrumpir el flujo del catálogo.
