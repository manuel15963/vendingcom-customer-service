-- ============================================================
-- VENDING.COM S.A.C — Módulo CUSTOMER
-- Índices (joins, búsqueda y "un solo principal por cliente")
-- ============================================================

-- Filtros del listado de clientes
CREATE INDEX IF NOT EXISTS idx_customers_status         ON customers(customer_status_id);
CREATE INDEX IF NOT EXISTS idx_customers_type           ON customers(customer_type_id);
-- Búsqueda por razón social insensible a mayúsculas
CREATE INDEX IF NOT EXISTS idx_customers_business_lower ON customers (LOWER(business_name));

-- Contactos por cliente + único principal activo por cliente
CREATE INDEX IF NOT EXISTS idx_customer_contacts_customer ON customer_contacts(customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_contacts_primary
    ON customer_contacts(customer_id) WHERE is_primary = TRUE;

-- Direcciones por cliente + única principal por cliente
CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer ON customer_addresses(customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_addresses_primary
    ON customer_addresses(customer_id) WHERE is_primary = TRUE;

-- Documentos por cliente + único principal por cliente
CREATE INDEX IF NOT EXISTS idx_customer_documents_customer ON customer_documents(customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_documents_primary
    ON customer_documents(customer_id) WHERE is_primary = TRUE;

-- Auditoría y catálogos
CREATE INDEX IF NOT EXISTS idx_customer_audit_logs_customer    ON customer_audit_logs(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_audit_logs_action_date ON customer_audit_logs(action_type, executed_at);
CREATE INDEX IF NOT EXISTS idx_customer_parameters_group       ON customer_parameters(parameter_group, parameter_status, sort_order);
