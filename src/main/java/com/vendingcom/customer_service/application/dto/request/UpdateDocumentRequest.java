package com.vendingcom.customer_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDocumentRequest(

        @NotNull(message = "El tipo de documento es obligatorio")
        Integer documentTypeId,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 50, message = "El número de documento no debe superar 50 caracteres")
        String documentNumber,

        @Size(max = 255, message = "La URL del archivo no debe superar 255 caracteres")
        String fileUrl,

        Boolean isPrimary
) {
}
