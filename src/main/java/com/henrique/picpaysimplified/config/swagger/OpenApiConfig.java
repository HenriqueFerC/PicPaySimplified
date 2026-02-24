package com.henrique.picpaysimplified.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(contact = @Contact(name = "Henrique Ferreira Cardoso", email = "henriqueferreiracardoso179411@hotmail.com"),
                title = "PicPaySimplified",
                description = "API para Transações Bancárias",
                version = "1.0"),
        servers = @Server(url = "http://localhost:8080"),
        security = @SecurityRequirement(name = "PicPayJWT")
)
@SecurityScheme(
        name = "picpayJwt",
        description = "Segurança JWT",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
