# Store Microservices - Products Service

Este microservicio es responsable de la gestión del catálogo de productos y de la autenticación de usuarios para la tienda de microservicios. Implementado bajo estándares **Senior**, incluye seguridad JWT, resiliencia con Resilience4j, migraciones con Flyway y observabilidad.

## Tecnologías
- **Java 22** + **Spring Boot 3.5.13**
- **Maven**
- **PostgreSQL**
- **Flyway** (Migraciones de DB)
- **Spring Security + JWT**
- **Resilience4j** (Circuit Breaker, Timeouts, Retries)
- **Bucket4j** (Rate Limiting)
- **Testcontainers** (Pruebas de Integración)
- **Pact** (Contract Testing)

---

## Cómo Correr el Proyecto

### Requisitos
- Docker y Docker Compose instalados.

### Paso 1: Levantar con Docker Compose
Desde la raíz del proyecto (donde se encuentra el `docker-compose.yml`):
```bash
docker-compose up --build
```
Esto levantará la base de datos PostgreSQL y el microservicio de Productos.

### Paso 2: Acceder a la documentación API
Una vez levantado, puedes acceder a la interfaz de Swagger UI en:
`http://localhost:8080/swagger-ui.html`

---

## Seguridad y Autenticación

El sistema cuenta con dos capas de seguridad:
1. **JWT**: Para peticiones desde el frontend.
2. **API Key**: Para comunicación entre microservicios (header `x-api-key`).

### Usuario por defecto (Semilla)
Al iniciar, Flyway inyecta automáticamente un usuario para pruebas:
- **Username**: `admin`
- **Password**: `admin`

### Ejemplo de Login y Uso de Token
1. **Obtener Token**:
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin"}'
   ```
2. **Usar el token** en peticiones de productos:
   ```bash
   curl -H "Authorization: Bearer <TU_TOKEN_AQUI>" http://localhost:8080/products
   ```

---

## Documentación Técnica Adicional
- [Decisiones Técnicas (DB, Resiliencia, Seguridad)](docs/Technical_Decisions.md)
- [Diagrama de Arquitectura (C4)](docs/Architecture.md)

---

## Pruebas y Calidad
Para ejecutar la suite completa de pruebas (Unitarias, Integración con Testcontainers y Contratos Pact):
```bash
mvn clean test
```

### Evidencia de Pruebas
```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## Variables de Entorno Principales
| Variable | Descripción | Valor Defecto |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | URL de la BD Postgres | `jdbc:postgresql://localhost:5432/productdb` |
| `INVENTORY_SERVICE_URL` | URL del microservicio B | `http://localhost:8081` |
| `INVENTORY_API_KEY` | Key para hablar con Inventario | `inventory-secret-key` |
| `JWT_SECRET` | Secreto para firmar tokens | `antigravity-super-secret-key-senior-test` |
