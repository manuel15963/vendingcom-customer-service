package com.vendingcom.customer_service.util.catalog;

/**
 * Conversión de códigos técnicos del catálogo a texto legible en español.
 * Usado al leer (no cambia la BD: el estado se sigue guardando por su parameter_id).
 */
public final class CatalogLabels {

    private CatalogLabels() {
    }

    /** Estado: ACTIVE → "ACTIVO", INACTIVE → "INACTIVO", etc. */
    public static String statusLabel(String statusCode) {
        if (statusCode == null) {
            return null;
        }
        return switch (statusCode) {
            case "ACTIVE" -> "ACTIVO";
            case "INACTIVE" -> "INACTIVO";
            case "SUSPENDED" -> "SUSPENDIDO";
            default -> statusCode;
        };
    }
}
