# Diagrama C4 (Nivel 1 y 2)

## C4 Nivel 1: Contexto de Sistema

Este diagrama ilustra a alto nivel cómo el usuario interactúa con nuestro sistema de Microservicios Store, y cómo éste a su vez se comunica con el sistema externo de Inventario.

```mermaid
C4Context
  title System Context Diagram para Store Microservices

  Person(customer, "Cliente", "Un cliente de la tienda que quiere comprar productos.")
  System(products_sys, "Products Service", "Permite listar productos, ver detalle y ejecutar compras.")
  System_Ext(inventory_sys, "Inventory Service", "Sistema externo que maneja el inventario disponible (mockeado en esta prueba).")

  Rel(customer, products_sys, "Busca productos y ejecuta compras usando", "REST/HTTP")
  Rel(products_sys, inventory_sys, "Verifica stock y solicita deducción usando", "REST/HTTP")
```

## C4 Nivel 2: Diagrama de Contenedores

Este diagrama profundiza dentro de nuestro "Products Service" para mostrar sus partes (API, Base de datos).

```mermaid
C4Container
  title Container Diagram para Products Service

  Person(customer, "Cliente", "Un cliente de la tienda.")

  System_Boundary(products_sys, "Products Service Boundary") {
    Container(api, "Products API", "Java, Spring Boot", "Provee la funcionalidad a través de una API REST (Controller -> Service).")
    ContainerDb(db, "Products DB", "PostgreSQL", "Almacena los registros de los productos.")
  }
  
  System_Ext(inventory_sys, "Inventory Service", "Microservicio externo de Inventario.")

  Rel(customer, api, "Llamadas REST", "JSON/HTTP")
  Rel(api, db, "Lee y escribe datos", "JDBC/JPA")
  Rel(api, inventory_sys, "Obtiene y deduce stock", "JSON/HTTP via RestClient")
```
