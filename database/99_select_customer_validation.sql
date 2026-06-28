-- ============================================================
-- VENDING.COM S.A.C — Módulo CUSTOMER
-- Consultas de verificación rápida (no modifican datos)
-- ============================================================

-- 1) Conteo de filas por tabla
SELECT 'customers'           AS tabla, COUNT(*) AS filas FROM customers
UNION ALL SELECT 'customer_contacts',   COUNT(*) FROM customer_contacts
UNION ALL SELECT 'customer_addresses',  COUNT(*) FROM customer_addresses
UNION ALL SELECT 'customer_documents',  COUNT(*) FROM customer_documents
UNION ALL SELECT 'customer_parameters', COUNT(*) FROM customer_parameters
UNION ALL SELECT 'customer_audit_logs', COUNT(*) FROM customer_audit_logs;

-- 2) Catálogos cargados (deberían ser 18 filas activas)
SELECT parameter_group, parameter_code, parameter_value, parameter_status
FROM customer_parameters
ORDER BY parameter_group, sort_order;

-- 3) Clientes con su tipo y estado en texto (verifica los JOIN a catálogos)
SELECT c.customer_id,
       c.business_name,
       t.parameter_value AS tipo,
       s.parameter_code  AS estado
FROM customers c
LEFT JOIN customer_parameters t ON t.parameter_id = c.customer_type_id
LEFT JOIN customer_parameters s ON s.parameter_id = c.customer_status_id
ORDER BY c.customer_id;
