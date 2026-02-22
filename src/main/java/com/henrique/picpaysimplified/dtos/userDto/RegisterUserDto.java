package com.henrique.picpaysimplified.dtos.userDto;

import com.henrique.picpaysimplified.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserDto(
        @NotBlank(message = "Full name is required")
        @Schema(description = "User's full name", example = "John Doe")
        String fullName,
        @NotBlank(message = "CPF/CNPJ is required")
        @Schema(description = "User's CPF or CNPJ", example = "123.456.789-00 or 12.345.678/0001-00")
        String cpfCnpj,
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        @Schema(description = "User's email address", example = "johndoe@email.com")
        String email,
        @NotBlank(message = "Password is required")
        @Schema(description = "User's password", example = "password123")
        String password,
        @NotBlank(message = "User type is required")
        @Schema(description = "Type of user (e.g, user, shopkeeper)", example = "user")
        TypeUser typeUser) {
}
