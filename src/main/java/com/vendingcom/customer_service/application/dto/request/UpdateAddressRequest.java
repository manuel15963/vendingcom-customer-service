package com.vendingcom.customer_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(

        @NotNull(message = "El tipo de dirección es obligatorio")
        Integer addressTypeId,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no debe superar 200 caracteres")
        String addressLine,

        @Size(max = 100, message = "El distrito no debe superar 100 caracteres")
        String district,

        @Size(max = 100, message = "La provincia no debe superar 100 caracteres")
        String province,

        @Size(max = 100, message = "El departamento no debe superar 100 caracteres")
        String department,

        @Size(max = 100, message = "El país no debe superar 100 caracteres")
        String country,

        @Size(max = 255, message = "La referencia no debe superar 255 caracteres")
        String reference,

        Boolean isPrimary
) {
}
