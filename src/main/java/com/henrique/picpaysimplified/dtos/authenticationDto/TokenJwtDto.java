package com.henrique.picpaysimplified.dtos.authenticationDto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenJwtDto(
        @Schema(description = "JWT token for authenticated user")
        String token) {
}
