package com.example.picheTest.service;

import com.example.picheTest.mapper.EntityMapper;
import com.example.picheTest.model.request.AccountCreateRQ;
import com.example.picheTest.repository.AccountRepository;
import com.example.picheTest.repository.entity.Account;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountService {
    @Autowired
    private AccountRepository repository;
    @Autowired
    private EntityMapper entityMapper;

    public List<Account> getAllAccounts() {
        return repository.findAll();
    }

    public Account getAccount(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).orElse(null);
    }

    public Account createAccount(AccountCreateRQ accountCreateRQ) {
        return repository.save(entityMapper.toAccount(accountCreateRQ));
    }

    public Account deposit(String accountId, BigDecimal amount) {
        Account account = repository.findByAccountNumber(accountId).orElse(null);
        if (account != null) {
            account.setBalance(account.getBalance().add(amount));
            repository.save(account);
        }
        return account;
    }

    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = repository.findByAccountNumber(accountNumber).orElse(null);
        if (account != null && account.getBalance().compareTo(amount) > 0) {
            account.setBalance(account.getBalance().subtract(amount));
            repository.save(account);
        }
        return account;
    }

    public boolean transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        Account fromAccount = repository.findByAccountNumber(fromAccountId).orElse(null);
        Account toAccount = repository.findByAccountNumber(toAccountId).orElse(null);
        if (fromAccount != null && toAccount != null && fromAccount.getBalance().compareTo(amount) > 0) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            repository.save(fromAccount);
            repository.save(toAccount);
            return true;
        }
        return false;
    }

}
