-- ============================================================
-- VENDING.COM S.A.C — Módulo CUSTOMER
-- DICCIONARIO DE DATOS — descripción de cada tabla y cada campo
-- (ejecutar después de 01_create_customer_tables.sql)
-- ============================================================

-- ------------------------------------------------------------
-- customer_parameters
-- ------------------------------------------------------------
COMMENT ON TABLE  customer_parameters                  IS 'Catálogo de tipos y estados del módulo de clientes (CUSTOMER_TYPE, CUSTOMER_STATUS, DOCUMENT_TYPE, ADDRESS_TYPE, etc.).';
COMMENT ON COLUMN customer_parameters.parameter_id     IS 'Identificador único del parámetro (clave primaria).';
COMMENT ON COLUMN customer_parameters.parameter_group  IS 'Grupo al que pertenece el parámetro. Ej: CUSTOMER_TYPE, DOCUMENT_TYPE.';
COMMENT ON COLUMN customer_parameters.parameter_code   IS 'Código técnico del parámetro dentro del grupo. Ej: EMPRESA, RUC, ACTIVE.';
COMMENT ON COLUMN customer_parameters.parameter_value  IS 'Valor o etiqueta del parámetro que se muestra/usa.';
COMMENT ON COLUMN customer_parameters.description      IS 'Descripción legible de para qué sirve el parámetro.';
COMMENT ON COLUMN customer_parameters.sort_order       IS 'Orden de aparición en listas y combos del frontend.';
COMMENT ON COLUMN customer_parameters.parameter_status IS 'Estado del parámetro: 0 = inactivo, 1 = activo.';
COMMENT ON COLUMN customer_parameters.created_at       IS 'Fecha y hora en que se creó el parámetro.';
COMMENT ON COLUMN customer_parameters.updated_at       IS 'Fecha y hora de la última modificación del parámetro.';

-- ------------------------------------------------------------
-- customers
-- ------------------------------------------------------------
COMMENT ON TABLE  customers                    IS 'Clientes (empresas, instituciones o personas) de la empresa de vending.';
COMMENT ON COLUMN customers.customer_id        IS 'Identificador único del cliente (clave primaria).';
COMMENT ON COLUMN customers.business_name      IS 'Razón social o nombre legal del cliente.';
COMMENT ON COLUMN customers.trade_name         IS 'Nombre comercial con el que opera el cliente.';
COMMENT ON COLUMN customers.customer_type_id   IS 'Tipo de cliente. FK a customer_parameters (grupo CUSTOMER_TYPE): EMPRESA, INSTITUCIÓN o PERSONA.';
COMMENT ON COLUMN customers.main_email         IS 'Correo electrónico principal del cliente.';
COMMENT ON COLUMN customers.main_phone         IS 'Teléfono principal del cliente.';
COMMENT ON COLUMN customers.website            IS 'Sitio web del cliente (opcional).';
COMMENT ON COLUMN customers.customer_status_id IS 'Estado del cliente. FK a customer_parameters (grupo CUSTOMER_STATUS): ACTIVO o INACTIVO.';
COMMENT ON COLUMN customers.created_by_user_id IS 'ID del usuario de auth-service que creó el cliente (no es FK: pertenece a otro microservicio).';
COMMENT ON COLUMN customers.updated_by_user_id IS 'ID del usuario de auth-service que modificó el cliente por última vez (no es FK).';
COMMENT ON COLUMN customers.created_at         IS 'Fecha y hora en que se registró el cliente.';
COMMENT ON COLUMN customers.updated_at         IS 'Fecha y hora de la última modificación del cliente.';

-- ------------------------------------------------------------
-- customer_contacts
-- ------------------------------------------------------------
COMMENT ON TABLE  customer_contacts                    IS 'Personas de contacto asociadas a cada cliente.';
COMMENT ON COLUMN customer_contacts.contact_id         IS 'Identificador único del contacto (clave primaria).';
COMMENT ON COLUMN customer_contacts.customer_id        IS 'Cliente al que pertenece el contacto. FK a customers.';
COMMENT ON COLUMN customer_contacts.full_name          IS 'Nombre completo de la persona de contacto.';
COMMENT ON COLUMN customer_contacts.position           IS 'Cargo o puesto del contacto en la empresa cliente.';
COMMENT ON COLUMN customer_contacts.email              IS 'Correo electrónico del contacto.';
COMMENT ON COLUMN customer_contacts.phone              IS 'Teléfono del contacto.';
COMMENT ON COLUMN customer_contacts.is_primary         IS 'Indica si es el contacto principal del cliente (solo uno por cliente).';
COMMENT ON COLUMN customer_contacts.contact_status_id  IS 'Estado del contacto. FK a customer_parameters (grupo CONTACT_STATUS).';
COMMENT ON COLUMN customer_contacts.created_by_user_id IS 'ID del usuario de auth-service que creó el contacto (no es FK).';
COMMENT ON COLUMN customer_contacts.updated_by_user_id IS 'ID del usuario de auth-service que modificó el contacto (no es FK).';
COMMENT ON COLUMN customer_contacts.created_at         IS 'Fecha y hora de creación del contacto.';
COMMENT ON COLUMN customer_contacts.updated_at         IS 'Fecha y hora de la última modificación del contacto.';

-- ------------------------------------------------------------
-- customer_addresses
-- ------------------------------------------------------------
COMMENT ON TABLE  customer_addresses                    IS 'Direcciones del cliente (fiscal, comercial, facturación).';
COMMENT ON COLUMN customer_addresses.address_id         IS 'Identificador único de la dirección (clave primaria).';
COMMENT ON COLUMN customer_addresses.customer_id        IS 'Cliente dueño de la dirección. FK a customers.';
COMMENT ON COLUMN customer_addresses.address_type_id    IS 'Tipo de dirección. FK a customer_parameters (grupo ADDRESS_TYPE): FISCAL, COMERCIAL o FACTURACIÓN.';
COMMENT ON COLUMN customer_addresses.address_line       IS 'Dirección detallada: calle, número, manzana, lote.';
COMMENT ON COLUMN customer_addresses.district           IS 'Distrito de la dirección.';
COMMENT ON COLUMN customer_addresses.province           IS 'Provincia de la dirección.';
COMMENT ON COLUMN customer_addresses.department         IS 'Departamento o región de la dirección.';
COMMENT ON COLUMN customer_addresses.country            IS 'País de la dirección (por defecto Perú).';
COMMENT ON COLUMN customer_addresses.reference          IS 'Referencia para ubicar el lugar (ej: frente al parque).';
COMMENT ON COLUMN customer_addresses.is_primary         IS 'Indica si es la dirección principal del cliente (solo una por cliente).';
COMMENT ON COLUMN customer_addresses.address_status_id  IS 'Estado de la dirección. FK a customer_parameters (grupo ADDRESS_STATUS).';
COMMENT ON COLUMN customer_addresses.created_by_user_id IS 'ID del usuario de auth-service que creó la dirección (no es FK).';
COMMENT ON COLUMN customer_addresses.updated_by_user_id IS 'ID del usuario de auth-service que modificó la dirección (no es FK).';
COMMENT ON COLUMN customer_addresses.created_at         IS 'Fecha y hora de creación de la dirección.';
COMMENT ON COLUMN customer_addresses.updated_at         IS 'Fecha y hora de la última modificación de la dirección.';

-- ------------------------------------------------------------
-- customer_documents
-- ------------------------------------------------------------
COMMENT ON TABLE  customer_documents                    IS 'Documentos legales del cliente (RUC, DNI, CE, pasaporte).';
COMMENT ON COLUMN customer_documents.document_id        IS 'Identificador único del documento (clave primaria).';
COMMENT ON COLUMN customer_documents.customer_id        IS 'Cliente dueño del documento. FK a customers.';
COMMENT ON COLUMN customer_documents.document_type_id   IS 'Tipo de documento. FK a customer_parameters (grupo DOCUMENT_TYPE): RUC, DNI, CE, PASAPORTE.';
COMMENT ON COLUMN customer_documents.document_number    IS 'Número del documento. Único por tipo (no se repite el mismo RUC/DNI).';
COMMENT ON COLUMN customer_documents.file_url           IS 'URL del archivo escaneado del documento (opcional).';
COMMENT ON COLUMN customer_documents.is_primary         IS 'Indica si es el documento principal del cliente (solo uno por cliente).';
COMMENT ON COLUMN customer_documents.document_status_id IS 'Estado del documento. FK a customer_parameters (grupo DOCUMENT_STATUS).';
COMMENT ON COLUMN customer_documents.created_by_user_id IS 'ID del usuario de auth-service que registró el documento (no es FK).';
COMMENT ON COLUMN customer_documents.updated_by_user_id IS 'ID del usuario de auth-service que modificó el documento (no es FK).';
COMMENT ON COLUMN customer_documents.created_at         IS 'Fecha y hora de registro del documento.';
COMMENT ON COLUMN customer_documents.updated_at         IS 'Fecha y hora de la última modificación del documento.';

-- ------------------------------------------------------------
-- customer_audit_logs
-- ------------------------------------------------------------
COMMENT ON TABLE  customer_audit_logs                     IS 'Auditoría de cambios del módulo de clientes. Solo se inserta, no se modifica (append-only).';
COMMENT ON COLUMN customer_audit_logs.audit_log_id        IS 'Identificador único del evento de auditoría (clave primaria).';
COMMENT ON COLUMN customer_audit_logs.customer_id         IS 'Cliente afectado por la acción (puede ser NULL si no aplica). FK a customers.';
COMMENT ON COLUMN customer_audit_logs.affected_table_name IS 'Nombre de la tabla afectada por la acción (customers, customer_contacts, etc.).';
COMMENT ON COLUMN customer_audit_logs.affected_record_id  IS 'ID del registro afectado dentro de la tabla indicada.';
COMMENT ON COLUMN customer_audit_logs.action_type         IS 'Tipo de acción auditada. Ej: CUSTOMER_CREATED, CONTACT_UPDATED, DOCUMENT_DEACTIVATED.';
COMMENT ON COLUMN customer_audit_logs.action_description  IS 'Descripción legible de la acción realizada.';
COMMENT ON COLUMN customer_audit_logs.old_data            IS 'Estado del registro ANTES del cambio, en formato JSON.';
COMMENT ON COLUMN customer_audit_logs.new_data            IS 'Estado del registro DESPUÉS del cambio, en formato JSON.';
COMMENT ON COLUMN customer_audit_logs.ip_address          IS 'Dirección IP desde donde se ejecutó la acción.';
COMMENT ON COLUMN customer_audit_logs.user_agent          IS 'Navegador o cliente desde donde se ejecutó la acción.';
COMMENT ON COLUMN customer_audit_logs.executed_by_user_id IS 'ID del usuario de auth-service que ejecutó la acción (no es FK).';
COMMENT ON COLUMN customer_audit_logs.executed_at         IS 'Fecha y hora en que ocurrió la acción auditada.';
