package com.vendingcom.customer_service.application.dto.response;

import java.util.List;

/**
 * Vista completa de un cliente con sus contactos, direcciones y documentos,
 * para construir la ficha del cliente en una sola llamada.
 */
public record CustomerDetailResponse(
        CustomerResponse customer,
        List<CustomerContactResponse> contacts,
        List<CustomerAddressResponse> addresses,
        List<CustomerDocumentResponse> documents
) {
}
