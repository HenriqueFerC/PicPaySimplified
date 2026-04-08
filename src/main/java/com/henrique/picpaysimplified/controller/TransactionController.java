package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.transactionDto.DetailsTransactionDto;
import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction Controller", description = "Endpoints for managing transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Perform a transaction", description = "Endpoint to perform a new transaction for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction successfully performed.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to perform a transaction."),
            @ApiResponse(responseCode = "409", description = "Bad request, invalid transaction data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsTransactionDto> toDoTransaction(@RequestBody @Valid RegisterTransactionalDto transactionalDto, UriComponentsBuilder uriBuilder) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var email = authentication.getName();
        var transaction = transactionService.transfer(email, transactionalDto);
        var uri = uriBuilder.path("transaction/{id}").buildAndExpand(transaction.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsTransactionDto(transaction));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw balance method", description = "Endpoint to perform a withdraw from the authenticated user's.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdraw successfully performed.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to perform a withdraw."),
            @ApiResponse(responseCode = "409", description = "Bad request, invalid withdraw data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsTransactionDto> withdraw(@RequestParam BigDecimal amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var transaction = transactionService.withdraw(email, amount);
        return ResponseEntity.ok().body(new DetailsTransactionDto(transaction));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit amount method", description = "Endpoint to perform a deposit to the authenticated user's.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit successfully performed.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to perform a deposit."),
            @ApiResponse(responseCode = "409", description = "Bad request, invalid deposit data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsTransactionDto> deposit(@RequestParam BigDecimal amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var transaction = transactionService.deposit(email, amount);
        return ResponseEntity.ok().body(new DetailsTransactionDto(transaction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find Transaction by Id", description = "Endpoint to retrieve a transaction performed by the id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the transactions.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user is not authenticated or transaction id not belongs of user."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsTransactionDto> detailsTransaction(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var email = authentication.getName();
        var detailsTranscation = transactionService.findTransactionById(id, email);
        return ResponseEntity.ok().body(detailsTranscation);
    }

    @GetMapping("/myTransactions")
    @Operation(summary = "List my transactions", description = "Endpoint to retrieve a paginated list of transactions performed by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transactions.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user is not authenticated."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<Page<DetailsTransactionDto>> listTransactions(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var email = authentication.getName();
        var list = transactionService.listTransactions(email, pageable);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/lastTransactions")
    @Operation(summary = "List last transactions", description = "Endpoint to retrieve a paginated" +
            " list of transactions performed in the last specified number of days for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transactions.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user is not authenticated."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<Page<DetailsTransactionDto>> listLastTransactions(@RequestParam @Valid Integer days, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var email = authentication.getName();
        var list = transactionService.listLastTransactions(days, email, pageable);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/revert/{id}")
    @Operation(summary = "Revert a transaction", description = "Endpoint to revert a specific transaction by its ID for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction successfully reverted.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to revert a transaction."),
            @ApiResponse(responseCode = "409", description = "Not found, transaction with the specified ID does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @SecurityRequirement(name = "picpayJwt")
    public ResponseEntity<DetailsTransactionDto> revertTransaction(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var email = authentication.getName();
        var transaction = transactionService.revertTransaction(email, id);
        return ResponseEntity.ok(new DetailsTransactionDto(transaction));
    }
}
