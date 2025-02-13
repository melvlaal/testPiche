package com.example.picheTest.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DepositRQ {
    private BigDecimal amount;
}