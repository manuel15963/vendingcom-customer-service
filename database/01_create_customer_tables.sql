-- ============================================================
-- VENDING.COM S.A.C  —  CUSTOMER-SERVICE
-- Base de datos: vendingcom_db  (LA MISMA que usa auth-service)
-- Módulo: CUSTOMER - Gestión de clientes  (PostgreSQL / Supabase)
-- ------------------------------------------------------------
-- Comparte la base con auth (mismo Postgres/Supabase), separados
-- por prefijo de tabla (customer_*). NO se crea la base aquí.
-- Orden: parameters primero (las demás lo referencian).
-- ============================================================

-- ------------------------------------------------------------
-- 1) CATÁLOGO DE PARÁMETROS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_parameters (
    parameter_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parameter_group   VARCHAR(50)  NOT NULL,
    parameter_code    VARCHAR(50)  NOT NULL,
    parameter_value   VARCHAR(100) NOT NULL,
    description       VARCHAR(255),
    sort_order        INTEGER      NOT NULL DEFAULT 1,
    parameter_status  SMALLINT     NOT NULL DEFAULT 1,   -- 0=INACTIVO, 1=ACTIVO
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT uq_customer_parameters_group_code UNIQUE (parameter_group, parameter_code),
    CONSTRAINT chk_customer_parameters_status CHECK (parameter_status IN (0,1))
);

-- ------------------------------------------------------------
-- 2) CLIENTES
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    business_name      VARCHAR(150) NOT NULL,   -- razón social / nombre legal
    trade_name         VARCHAR(150),            -- nombre comercial
    customer_type_id   INTEGER NOT NULL,        -- FK -> customer_parameters (CUSTOMER_TYPE)
    main_email         VARCHAR(120),
    main_phone         VARCHAR(20),
    website            VARCHAR(150),
    customer_status_id INTEGER NOT NULL,        -- FK -> customer_parameters (CUSTOMER_STATUS)
    created_by_user_id INTEGER,                 -- usuario de auth-service (no FK)
    updated_by_user_id INTEGER,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    CONSTRAINT fk_customers_type   FOREIGN KEY (customer_type_id)   REFERENCES customer_parameters(parameter_id),
    CONSTRAINT fk_customers_status FOREIGN KEY (customer_status_id) REFERENCES customer_parameters(parameter_id)
);

-- ------------------------------------------------------------
-- 3) CONTACTOS DEL CLIENTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_contacts (
    contact_id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id        INTEGER NOT NULL,
    full_name          VARCHAR(150) NOT NULL,
    position           VARCHAR(100),
    email              VARCHAR(120),
    phone              VARCHAR(20),
    is_primary         BOOLEAN NOT NULL DEFAULT FALSE,
    contact_status_id  INTEGER NOT NULL,        -- FK -> parameters (CONTACT_STATUS)
    created_by_user_id INTEGER,
    updated_by_user_id INTEGER,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    CONSTRAINT fk_customer_contacts_customer FOREIGN KEY (customer_id)       REFERENCES customers(customer_id),
    CONSTRAINT fk_customer_contacts_status   FOREIGN KEY (contact_status_id) REFERENCES customer_parameters(parameter_id)
);

-- ------------------------------------------------------------
-- 4) DIRECCIONES DEL CLIENTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_addresses (
    address_id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id        INTEGER NOT NULL,
    address_type_id    INTEGER NOT NULL,        -- FK -> parameters (ADDRESS_TYPE)
    address_line       VARCHAR(200) NOT NULL,
    district           VARCHAR(100),
    province           VARCHAR(100),
    department         VARCHAR(100),
    country            VARCHAR(100) NOT NULL DEFAULT 'Perú',
    reference          VARCHAR(255),
    is_primary         BOOLEAN NOT NULL DEFAULT FALSE,
    address_status_id  INTEGER NOT NULL,        -- FK -> parameters (ADDRESS_STATUS)
    created_by_user_id INTEGER,
    updated_by_user_id INTEGER,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id)       REFERENCES customers(customer_id),
    CONSTRAINT fk_customer_addresses_type     FOREIGN KEY (address_type_id)   REFERENCES customer_parameters(parameter_id),
    CONSTRAINT fk_customer_addresses_status   FOREIGN KEY (address_status_id) REFERENCES customer_parameters(parameter_id)
);

-- ------------------------------------------------------------
-- 5) DOCUMENTOS DEL CLIENTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_documents (
    document_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id        INTEGER NOT NULL,
    document_type_id   INTEGER NOT NULL,        -- FK -> parameters (DOCUMENT_TYPE)
    document_number    VARCHAR(50) NOT NULL,
    file_url           VARCHAR(255),
    is_primary         BOOLEAN NOT NULL DEFAULT FALSE,
    document_status_id INTEGER NOT NULL,        -- FK -> parameters (DOCUMENT_STATUS)
    created_by_user_id INTEGER,
    updated_by_user_id INTEGER,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    CONSTRAINT fk_customer_documents_customer FOREIGN KEY (customer_id)        REFERENCES customers(customer_id),
    CONSTRAINT fk_customer_documents_type     FOREIGN KEY (document_type_id)   REFERENCES customer_parameters(parameter_id),
    CONSTRAINT fk_customer_documents_status   FOREIGN KEY (document_status_id) REFERENCES customer_parameters(parameter_id),
    CONSTRAINT uq_customer_documents_number   UNIQUE (document_type_id, document_number)  -- evita RUC/DNI duplicado
);

-- ------------------------------------------------------------
-- 6) AUDITORÍA DEL MÓDULO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_audit_logs (
    audit_log_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id         INTEGER,                 -- cliente afectado (puede ser NULL)
    affected_table_name VARCHAR(50),
    affected_record_id  INTEGER,
    action_type         VARCHAR(50) NOT NULL,    -- CUSTOMER_CREATED, CONTACT_UPDATED, ...
    action_description  TEXT,
    old_data            JSONB,
    new_data            JSONB,
    ip_address          VARCHAR(45),
    user_agent          VARCHAR(255),
    executed_by_user_id INTEGER,                 -- usuario de auth-service (no FK)
    executed_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_audit_logs_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);
