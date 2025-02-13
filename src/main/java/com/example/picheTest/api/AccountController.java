package com.example.picheTest.api;

import com.example.picheTest.model.request.AccountCreateRQ;
import com.example.picheTest.model.request.DepositRQ;
import com.example.picheTest.model.request.TransferRQ;
import com.example.picheTest.model.request.WithdrawRQ;
import com.example.picheTest.repository.entity.Account;
import com.example.picheTest.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
@Tag(name = "Account Management", description = "Operations related to bank accounts")
class AccountController {
    @Autowired
    private AccountService service;

    @GetMapping
    @Operation(summary = "List all accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                    content = @Content(schema = @Schema(implementation = Account.class))),
            @ApiResponse(responseCode = "204", description = "No accounts found")
    })
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = service.getAllAccounts();
        if (accounts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details by Account Number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account details",
                    content = @Content(schema = @Schema(implementation = Account.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok().body(service.getAccount(accountNumber));
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created account",
                    content = @Content(schema = @Schema(implementation = Account.class))),
            @ApiResponse(responseCode = "422", description = "Bad request, invalid input")
    })
    public ResponseEntity<Account> createAccount(@RequestBody AccountCreateRQ accountCreateRQ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(accountCreateRQ));
    }

    @PostMapping("/{accountNumber}/deposit")
    @Operation(summary = "Deposit funds into an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deposited funds",
                    content = @Content(schema = @Schema(implementation = Account.class))),
            @ApiResponse(responseCode = "422", description = "Invalid deposit request"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> deposit(@PathVariable String accountNumber, @RequestBody DepositRQ depositRQ) {
        return ResponseEntity.ok().body(service.deposit(accountNumber, depositRQ));
    }

    @PostMapping("/{accountNumber}/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully withdrew funds",
                    content = @Content(schema = @Schema(implementation = Account.class))),
            @ApiResponse(responseCode = "422", description = "Invalid withdraw request"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> withdraw(@PathVariable String accountNumber, @RequestBody WithdrawRQ withdrawRQ) {
        return ResponseEntity.ok().body(service.withdraw(accountNumber, withdrawRQ));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully transferred funds",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "422", description = "Invalid transfer request"),
            @ApiResponse(responseCode = "404", description = "One or more accounts not found")
    })
    public ResponseEntity<Boolean> transfer(@RequestBody TransferRQ transferRQ) {
        return ResponseEntity.ok().body(service.transfer(transferRQ));
    }
}

