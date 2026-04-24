# Products Service - Store Microservices

Este es el microservicio de **Productos**, parte de la arquitectura de la tienda (Store Microservices). Está construido con Java 22, Spring Boot 3 y PostgreSQL, siguiendo principios de Clean Architecture.

## Tecnologías Principales
- **Java 22** & **Spring Boot 3.5.13**
- **Spring Data JPA** & **PostgreSQL**
- **Resilience4j** (Manejo de Reintentos)
- **Testcontainers**, **Mockito**, **JUnit 5** y **Pact** (Testing)
- **Springdoc OpenAPI** (Swagger)

## Prerrequisitos
- Tener **Docker** y **Docker Compose** instalados.
- Tener **Java 22** instalado localmente si deseas compilar con Maven fuera del contenedor.

## Instrucciones para levantar el proyecto

### 1. Levantar la Base de Datos
En la carpeta raíz del proyecto, donde se encuentra tu archivo `docker-compose.yml`, ejecuta el siguiente comando para levantar el contenedor de PostgreSQL:

```bash
docker-compose up -d
```
Esto levantará PostgreSQL en el puerto `5433` (mapeado desde `5432` internamente) usando el usuario, contraseña y base de datos predeterminados en el compose (`postgres`/`postgres`/`store`).

### 2. Variables de Entorno (Opcional)
La aplicación viene preconfigurada para conectarse a `localhost:5433`. Si deseas sobreescribir configuraciones, puedes usar las siguientes variables de entorno:
- `SPRING_DATASOURCE_URL`: (Ej. `jdbc:postgresql://localhost:5433/store`)
- `SPRING_DATASOURCE_USERNAME`: (Ej. `postgres`)
- `SPRING_DATASOURCE_PASSWORD`: (Ej. `postgres`)
- `INVENTORY_SERVICE_URL`: (Ej. `http://localhost:8081` - Usado por el InventoryClient)

### 3. Ejecutar la Aplicación
Usando el Maven Wrapper incluido en el proyecto, levanta la aplicación:

```bash
cd products-service/
./mvnw spring-boot:run
```
La aplicación estará disponible en `http://localhost:8080`.

## Documentación de la API (Swagger)
Una vez que la aplicación esté corriendo, puedes explorar todos los endpoints y DTOs en la interfaz interactiva de Swagger:
**URL:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Pruebas (Postman o cURL)
Hemos proveído un archivo de colección listo para importar:
* **Archivo:** `Products.postman_collection.json`
* **Instrucciones:** Abre Postman -> Importar -> Arrastra el archivo JSON.

### Ejemplos rápidos con cURL:

**Crear un Producto**
```bash
curl -X POST http://localhost:8080/products \
-H "Content-Type: application/json" \
-d '{
  "sku": "PROD-001",
  "name": "Teclado Mecánico",
  "price": 120.50,
  "status": "ACTIVE"
}'
```

**Listar Productos (Paginado)**
```bash
curl -X GET "http://localhost:8080/products?page=0&size=10"
```

## Arquitectura y Decisiones Técnicas
Consulta la carpeta `/docs` para ver en detalle los diagramas C4 y las justificaciones técnicas detrás de la elección de librerías y patrones de resiliencia:
- [Diagramas C4 Model](docs/C4_Model.md)
- [Decisiones Técnicas](docs/Technical_Decisions.md)

## Ejecutar Suite de Pruebas
Para ejecutar las pruebas Unitarias, de Integración (que levantan Testcontainers) y las pruebas de Contrato (Pact), ejecuta:
```bash
./mvnw clean test
```
