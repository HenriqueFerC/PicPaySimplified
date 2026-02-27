package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.transactionDto.DetailsTransactionDto;
import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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
            @ApiResponse(responseCode = "404", description = "Bad request, invalid transaction data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsTransactionDto> toDoTransaction(@RequestBody RegisterTransactionalDto transactionalDto, Authentication authentication, UriComponentsBuilder uriBuilder) {
        var transaction = transactionService.registerTransaction(authentication, transactionalDto);
        var uri = uriBuilder.path("transaction/{id}").buildAndExpand(transaction.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsTransactionDto(transaction));
    }

    @GetMapping("/myTransactions")
    @Operation(summary = "List my transactions", description = "Endpoint to retrieve a paginated list of transactions performed by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transactions.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user is not authenticated."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<Page<DetailsTransactionDto>> listTransactions(Authentication authentication, Pageable pageable) {
        var list = transactionService.listTransactions(authentication, pageable);
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
    public ResponseEntity<Page<DetailsTransactionDto>> listLastTransactions(@RequestParam Integer days,Authentication authentication,Pageable pageable) {
        var list = transactionService.listLastTransactions(days, authentication ,pageable);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/revert/{id}")
    @Operation(summary = "Revert a transaction", description = "Endpoint to revert a specific transaction by its ID for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction successfully reverted.",
                    content = @Content(schema = @Schema(implementation = DetailsTransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user must be authenticated to revert a transaction."),
            @ApiResponse(responseCode = "409", description = "Not found, transaction with the specified ID does not exist."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsTransactionDto> revertTransaction(@PathVariable Integer id, Authentication authentication) {
        var transaction = transactionService.revertTransaction(authentication, id);
        return ResponseEntity.ok(new DetailsTransactionDto(transaction));
    }
}
