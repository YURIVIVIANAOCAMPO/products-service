# Arquitectura del Sistema (C4 Nivel 2)

El siguiente diagrama describe la interacción entre los contenedores del sistema StoreMaster, los usuarios y los límites del sistema.

```mermaid
graph TD
    User((Usuario/Admin))
    
    subgraph "StoreMaster System (Cloud/Docker)"
        Frontend[Vue.js Frontend Dashboard]
        
        subgraph "Backend Services"
            ProductService[Products Microservice]
            InventoryService[Inventory Microservice]
        end
        
        Database[(PostgreSQL Database)]
    end
    
    User -->|Interactúa via HTTPS| Frontend
    Frontend -->|API REST + JWT| ProductService
    Frontend -->|API REST + API Key| InventoryService
    
    ProductService -->|Consulta Stock / Sync| InventoryService
    InventoryService -->|Sincroniza Estado| ProductService
    
    ProductService -->|Lee/Escribe| Database
    InventoryService -->|Lee/Escribe| Database
    
    style User fill:#f9f,stroke:#333,stroke-width:2px
    style Frontend fill:#69f,stroke:#333,stroke-width:2px
    style ProductService fill:#00ED64,stroke:#333,stroke-width:2px
    style InventoryService fill:#00ED64,stroke:#333,stroke-width:2px
    style Database fill:#ccc,stroke:#333,stroke-width:2px
```

### Componentes principales:
1.  **Frontend Dashboard:** Aplicación SPA que consume los servicios.
2.  **Products Microservice:** Catálogo de productos, autenticación JWT y exposición de APIs
3.  **Inventory Microservice:** Especialista en lógica de almacén y concurrencia.
4.  **Shared Database:** Instancia de PostgreSQL con esquemas separados gestionados por Flyway.
