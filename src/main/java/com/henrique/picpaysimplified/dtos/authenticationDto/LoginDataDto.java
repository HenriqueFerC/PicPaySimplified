package com.henrique.picpaysimplified.dtos.authenticationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginDataDto(
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        @Schema(description = "User's email address", example = "abcd@email.com")
        String email,
        @NotBlank(message = "Password is required")
        @Schema(description = "User's password", example = "password123")
        String password,
        @NotNull(message = "Remember me is required")
        @Schema(description = "Remember authentication", example = "true")
        boolean rememberMe) {
}
