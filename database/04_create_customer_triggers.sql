-- ============================================================
-- VENDING.COM S.A.C — Módulo CUSTOMER
-- Trigger que actualiza updated_at en cada UPDATE
-- ============================================================

CREATE OR REPLACE FUNCTION fn_customer_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_customers_updated_at ON customers;
CREATE TRIGGER trg_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION fn_customer_set_updated_at();

DROP TRIGGER IF EXISTS trg_customer_contacts_updated_at ON customer_contacts;
CREATE TRIGGER trg_customer_contacts_updated_at BEFORE UPDATE ON customer_contacts
    FOR EACH ROW EXECUTE FUNCTION fn_customer_set_updated_at();

DROP TRIGGER IF EXISTS trg_customer_addresses_updated_at ON customer_addresses;
CREATE TRIGGER trg_customer_addresses_updated_at BEFORE UPDATE ON customer_addresses
    FOR EACH ROW EXECUTE FUNCTION fn_customer_set_updated_at();

DROP TRIGGER IF EXISTS trg_customer_documents_updated_at ON customer_documents;
CREATE TRIGGER trg_customer_documents_updated_at BEFORE UPDATE ON customer_documents
    FOR EACH ROW EXECUTE FUNCTION fn_customer_set_updated_at();

DROP TRIGGER IF EXISTS trg_customer_parameters_updated_at ON customer_parameters;
CREATE TRIGGER trg_customer_parameters_updated_at BEFORE UPDATE ON customer_parameters
    FOR EACH ROW EXECUTE FUNCTION fn_customer_set_updated_at();
