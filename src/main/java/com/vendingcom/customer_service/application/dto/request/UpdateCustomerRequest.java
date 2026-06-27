package com.vendingcom.customer_service.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150, message = "La razón social no debe superar 150 caracteres")
        String businessName,

        @Size(max = 150, message = "El nombre comercial no debe superar 150 caracteres")
        String tradeName,

        @NotNull(message = "El tipo de cliente es obligatorio")
        Integer customerTypeId,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no debe superar 120 caracteres")
        String mainEmail,

        @Size(max = 20, message = "El teléfono no debe superar 20 caracteres")
        String mainPhone,

        @Size(max = 150, message = "El sitio web no debe superar 150 caracteres")
        String website
) {
}
