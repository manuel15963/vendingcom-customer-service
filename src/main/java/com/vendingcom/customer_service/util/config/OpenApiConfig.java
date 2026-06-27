package com.vendingcom.customer_service.util.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vendingComCustomerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VendingCom Customer Service API")
                        .version("1.0.0")
                        .description("""
                                Microservicio de gestión de clientes de VendingCom.

                                Incluye:
                                - Registro y consulta de clientes
                                - Contactos, direcciones y documentos del cliente
                                - Catálogos del módulo
                                - Auditoría de cambios
                                """)
                        .contact(new Contact()
                                .name("VendingCom")
                                .email("adolfo.berrocal@vallegrande.edu.pe"))
                        .license(new License()
                                .name("Proyecto universitario")
                                .url("https://vendingcom.local")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
