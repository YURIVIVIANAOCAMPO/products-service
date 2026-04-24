# Store Microservices - Ecosystem

Este ecosistema está compuesto por dos microservicios diseñados bajo estándares **Senior** para una tienda escalable.

## 🚀 Arquitectura
1.  **Products Service (Puerto 8080)**: Gestión de catálogo y autenticación (JWT).
2.  **Inventory Service (Puerto 8081)**: Gestión de stock físico y compras con control de concurrencia.

## 🛠️ Cómo Correr el Proyecto

### Requisitos
- Docker y Docker Compose instalados.

### Paso Único: Levantar todo el ecosistema
Desde la raíz del proyecto:
```bash
docker-compose up --build
```
Esto levantará:
- `products-db` (Postgres 5432)
- `inventory-db` (Postgres 5433)
- `products-service` (8080)
- `inventory-service` (8081)

---

## 🔐 Seguridad y Autenticación

### Comunicación Inter-Servicios (API Key)
Los servicios se comunican usando el header `x-api-key`.
- Key configurada: `inventory-secret-key`

### Acceso Externo (JWT)
El `products-service` emite tokens JWT para el frontend.
- **Login**: `POST /auth/login` con `admin`/`admin`.

---

## 📦 Uso de la API (Endpoints Principales)

### 1. Comprar un Producto
Para realizar una compra segura (idempotente y protegida contra concurrencia):
```bash
curl -X POST http://localhost:8081/inventory/purchases \
  -H "x-api-key: inventory-secret-key" \
  -H "Idempotency-Key: compra-unica-123" \
  -H "Content-Type: application/json" \
  -d '{"productId": "550e8400-e29b-41d4-a716-446655440000", "quantity": 1}'
```

### 2. Consultar Productos
```bash
curl http://localhost:8080/products
```

---

## 🧪 Pruebas y Calidad
Ambos servicios cuentan con pruebas de integración usando **Testcontainers**.
Para ejecutar pruebas en un servicio específico:
```bash
cd products-service && ./mvnw test
# o
cd inventory-service && ./mvnw test
```

---

## 📁 Documentación Técnica
- [Decisiones Técnicas (Concurrencia, Idempotencia, Resiliencia)](products-service/docs/Technical_Decisions.md)
- [Diagrama de Arquitectura](products-service/docs/Architecture.md)
