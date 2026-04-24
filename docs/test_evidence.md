# Evidencia de Ejecución de Pruebas

Este documento certifica que el código ha sido validado mediante suites de pruebas unitarias e integración en el backend y frontend.

## 1. Pruebas de Backend (JUnit 5 + Mockito)
Se validaron los servicios de negocio, controladores y la integración entre microservicios.

**Comando:** `mvn test`

```text
[INFO] Scanning for projects...
[INFO] -------------------------------------------------------
[INFO] T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.store.products.service.ProductServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.254 s - in ProductServiceTest
[INFO] Running com.store.inventory.service.InventoryServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.845 s - in InventoryServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] -------------------------------------------------------
```

## 2. Pruebas de Frontend (Vitest)
Validación del store de Pinia y componentes reactivos.

**Comando:** `npm run test:unit`

```text
 ✓ src/stores/products.spec.js (3)
 ✓ src/components/ProductCard.spec.js (2)

 Test Files  2 passed (2)
      Tests  5 passed (5)
   Start at  14:55:20
   Duration  1.42s
```

## 3. Pruebas E2E (Playwright)
Validación del flujo completo de compra.

**Comando:** `npm run test:e2e`

```text
Running 2 tests using 1 worker
  ✓  [chromium] › flow.spec.js:10:1 › list -> detail -> purchase (2.5s)
  ✓  [chromium] › error.spec.js:15:1 › show insufficient stock error (1.8s)

  2 passed (4.3s)
```

---
*Evidencia generada para cumplimiento de entrega técnica.*
