package com.example.picheTest.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferRQ {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
}