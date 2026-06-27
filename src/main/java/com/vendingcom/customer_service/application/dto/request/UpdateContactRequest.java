package com.vendingcom.customer_service.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateContactRequest(

        @NotBlank(message = "El nombre del contacto es obligatorio")
        @Size(max = 150, message = "El nombre no debe superar 150 caracteres")
        String fullName,

        @Size(max = 100, message = "El cargo no debe superar 100 caracteres")
        String position,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no debe superar 120 caracteres")
        String email,

        @Size(max = 20, message = "El teléfono no debe superar 20 caracteres")
        String phone,

        Boolean isPrimary
) {
}
