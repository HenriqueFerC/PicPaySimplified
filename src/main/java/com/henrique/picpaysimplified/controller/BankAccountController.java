package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.config.security.JwtUtil;
import com.henrique.picpaysimplified.dtos.bankAccountDto.DetailsBankAccountDto;
import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/bankAccount")
@RequiredArgsConstructor
@Tag(name = "Bank Account Controller", description = "Endpoints for bank account management")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new bank account", description = "Endpoint to register a new bank account for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bank account successfully registered.",
                    content = @Content(schema = @Schema(implementation = DetailsBankAccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to register a bank account."),
            @ApiResponse(responseCode = "409", description = "Bad request, invalid bank account data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsBankAccountDto> registerBankAccount(@RequestBody @Valid RegisterBankAccountDto bankAccountDto, UriComponentsBuilder uriBuilder) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var bankAccount = bankAccountService.registerBankAccount(email, bankAccountDto);
        var uri = uriBuilder.path("bankAccount/{id}").buildAndExpand(bankAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsBankAccountDto(bankAccount));
    }

    @GetMapping("/myBankAccount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "BankAccount informations returned with successfully.",
                    content = @Content(schema = @Schema(implementation = DetailsBankAccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden access, user must be authenticated to perform a deposit."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsBankAccountDto> bankAccountDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var bankAccount = bankAccountService.findBankAccountByUser(email);
        return ResponseEntity.ok().body(new DetailsBankAccountDto(bankAccount));
    }
}
