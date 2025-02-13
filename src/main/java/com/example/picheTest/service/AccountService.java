package com.example.picheTest.service;

import com.example.picheTest.mapper.EntityMapper;
import com.example.picheTest.model.request.AccountCreateRQ;
import com.example.picheTest.model.request.DepositRQ;
import com.example.picheTest.model.request.TransferRQ;
import com.example.picheTest.model.request.WithdrawRQ;
import com.example.picheTest.repository.AccountRepository;
import com.example.picheTest.repository.entity.Account;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
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
        return repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account with account number " + accountNumber + " not found"));
    }

    public Account createAccount(AccountCreateRQ accountCreateRQ) {
        try {
            Account account = entityMapper.toAccount(accountCreateRQ);
            return repository.save(account);
        } catch (DataIntegrityViolationException e) {
            System.out.println("Account number already exists: " + accountCreateRQ.getAccountNumber());
            throw new IllegalArgumentException("Account number already exists: " + accountCreateRQ.getAccountNumber());
        }
    }

    public Account deposit(String accountNumber, DepositRQ depositRQ) {
        Account account = this.getAccount(accountNumber);

        if (depositRQ == null || depositRQ.getAmount() == null) {
            throw new IllegalArgumentException("Amount must be provided");
        }

        account.setBalance(account.getBalance().add(depositRQ.getAmount()));
        repository.save(account);

        return account;
    }

    public Account withdraw(String accountNumber, WithdrawRQ withdrawRQ) {
        Account account = this.getAccount(accountNumber);
        if (withdrawRQ == null || withdrawRQ.getAmount() == null) {
            throw new IllegalArgumentException("Amount must be provided");
        }
        if (account != null && account.getBalance().compareTo(withdrawRQ.getAmount()) >= 0) {
            account.setBalance(account.getBalance().subtract(withdrawRQ.getAmount()));
            repository.save(account);
            return account;
        }
        else {
            throw new IllegalArgumentException("Account " + accountNumber + " does not have sufficient balance");
        }

    }

    @Transactional
    public boolean transfer(TransferRQ transferRQ) {
        if (transferRQ == null || transferRQ.getFromAccountNumber() == null) {
            throw new IllegalArgumentException("From Account Number must be provided");
        }
        if (transferRQ.getToAccountNumber() == null) {
            throw new IllegalArgumentException("To Account Number must be provided");
        }
        if (transferRQ.getToAccountNumber().equals(transferRQ.getFromAccountNumber())) {
            throw new IllegalArgumentException("To Account Number and From Account Number must be different");
        }
        if (transferRQ.getAmount() == null) {
            throw new IllegalArgumentException("Amount must be provided");
        }

        Account fromAccount = this.getAccount(transferRQ.getFromAccountNumber());
        Account toAccount = this.getAccount(transferRQ.getToAccountNumber());
        if (fromAccount.getBalance().compareTo(transferRQ.getAmount()) > 0) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(transferRQ.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(transferRQ.getAmount()));
            repository.save(fromAccount);
            repository.save(toAccount);
            return true;
        }
        else {
            throw new IllegalArgumentException("Account " + transferRQ.getFromAccountNumber() + " does not have sufficient balance");
        }
    }

}
