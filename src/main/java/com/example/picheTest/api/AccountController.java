package com.example.picheTest.api;

import com.example.picheTest.model.request.AccountCreateRQ;
import com.example.picheTest.repository.entity.Account;
import com.example.picheTest.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public List<Account> getAllAccounts() {
        return service.getAllAccounts();
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details by Account Number")
    public Account getAccount(@PathVariable String accountNumber) {
        return service.getAccount(accountNumber);
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public Account createAccount(@RequestBody AccountCreateRQ accountCreateRQ) {
        return service.createAccount(accountCreateRQ);
    }

    @PostMapping("/{accountNumber}/deposit")
    @Operation(summary = "Deposit funds into an account")
    public Account deposit(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        return service.deposit(accountNumber, amount);
    }

    @PostMapping("/{accountNumber}/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    public Account withdraw(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        return service.withdraw(accountNumber, amount);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    public boolean transfer(@RequestParam String fromAccountNumber, @RequestParam String toAccountNumber, @RequestParam BigDecimal amount) {
        return service.transfer(fromAccountNumber, toAccountNumber, amount);
    }
}

