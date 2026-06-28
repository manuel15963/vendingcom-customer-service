# Base de datos — CUSTOMER-SERVICE

Esquema PostgreSQL del módulo de clientes. Comparte la base con **auth-service**
(mismo Postgres / Supabase, base `vendingcom_db`), separado por prefijo `customer_*`.

> La aplicación **no** crea las tablas sola (`spring.sql.init.mode=never`).
> Este SQL es la **fuente de verdad** del esquema y sirve para levantarlo en local.

## Cómo correrlo en local

Ejecuta los archivos **en orden**:

| # | Archivo | Qué hace |
|---|---|---|
| 1 | `01_create_customer_tables.sql`   | Tablas + claves foráneas + constraints |
| 2 | `02_create_customer_comments.sql` | Diccionario de datos (COMMENT ON de cada tabla/campo) |
| 3 | `03_create_customer_indexes.sql`  | Índices (joins, búsqueda y "un solo principal" por cliente) |
| 4 | `04_create_customer_triggers.sql` | Trigger que mantiene `updated_at` |
| 5 | `05_insert_customer_seed_data.sql`| Catálogos base (tipos y estados) — idempotente |
| 6 | `99_select_customer_validation.sql` | Consultas de verificación (opcional) |

### Con psql
```bash
psql "<connection-string>" -f 01_create_customer_tables.sql
psql "<connection-string>" -f 02_create_customer_comments.sql
psql "<connection-string>" -f 03_create_customer_indexes.sql
psql "<connection-string>" -f 04_create_customer_triggers.sql
psql "<connection-string>" -f 05_insert_customer_seed_data.sql
```

### Con Supabase
Pega el contenido de cada archivo en el **SQL Editor**, en el mismo orden.

## Notas
- `created_by_user_id` / `updated_by_user_id` / `executed_by_user_id` son IDs de
  usuarios de **auth-service**: enteros simples, **sin FK** entre microservicios.
- El seed usa `ON CONFLICT DO NOTHING`, así que se puede volver a correr sin duplicar.
- Hay un índice **único parcial** por `is_primary = TRUE`: solo puede haber **un**
  contacto / dirección / documento principal por cliente.
