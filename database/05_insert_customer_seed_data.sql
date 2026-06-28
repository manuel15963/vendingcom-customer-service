-- ============================================================
-- VENDING.COM S.A.C — Módulo CUSTOMER
-- Catálogos base (customer_parameters)
-- Idempotente: ON CONFLICT DO NOTHING (se puede correr varias veces).
-- Orden -> parameter_id: CUSTOMER_STATUS 1-2 · CUSTOMER_TYPE 3-5 ·
--   CONTACT_STATUS 6-7 · ADDRESS_TYPE 8-10 · ADDRESS_STATUS 11-12 ·
--   DOCUMENT_TYPE 13-16 · DOCUMENT_STATUS 17-18
-- ============================================================

INSERT INTO customer_parameters (parameter_group, parameter_code, parameter_value, description, sort_order, parameter_status) VALUES
('CUSTOMER_STATUS','ACTIVE','1','Cliente activo, operando en el sistema.',1,1),
('CUSTOMER_STATUS','INACTIVE','0','Cliente deshabilitado administrativamente.',2,1),

('CUSTOMER_TYPE','EMPRESA','EMPRESA','Persona jurídica con RUC.',1,1),
('CUSTOMER_TYPE','INSTITUCION','INSTITUCIÓN','Entidad pública o educativa.',2,1),
('CUSTOMER_TYPE','PERSONA','PERSONA','Persona natural.',3,1),

('CONTACT_STATUS','ACTIVE','1','Contacto vigente.',1,1),
('CONTACT_STATUS','INACTIVE','0','Contacto deshabilitado.',2,1),

('ADDRESS_TYPE','FISCAL','FISCAL','Domicilio fiscal del cliente.',1,1),
('ADDRESS_TYPE','COMERCIAL','COMERCIAL','Dirección comercial u operativa.',2,1),
('ADDRESS_TYPE','FACTURACION','FACTURACIÓN','Dirección para facturación.',3,1),

('ADDRESS_STATUS','ACTIVE','1','Dirección vigente.',1,1),
('ADDRESS_STATUS','INACTIVE','0','Dirección deshabilitada.',2,1),

('DOCUMENT_TYPE','RUC','RUC','Registro Único de Contribuyentes.',1,1),
('DOCUMENT_TYPE','DNI','DNI','Documento Nacional de Identidad.',2,1),
('DOCUMENT_TYPE','CE','Carnet de Extranjería','Documento para extranjeros.',3,1),
('DOCUMENT_TYPE','PASAPORTE','Pasaporte','Documento internacional.',4,1),

('DOCUMENT_STATUS','ACTIVE','1','Documento vigente.',1,1),
('DOCUMENT_STATUS','INACTIVE','0','Documento deshabilitado.',2,1)
ON CONFLICT (parameter_group, parameter_code) DO NOTHING;
