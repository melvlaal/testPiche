package com.example.picheTest.repository;

import com.example.picheTest.repository.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByAccountIdFrom(Long accountIdFrom);

    List<TransactionHistory> findByAccountIdTo(Long accountIdTo);
}
