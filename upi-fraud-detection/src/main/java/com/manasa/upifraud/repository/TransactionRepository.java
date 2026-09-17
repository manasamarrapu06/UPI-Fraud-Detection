package com.manasa.upifraud.repository;

import com.manasa.upifraud.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // Get transactions belonging to a specific user
    List<Transaction> findByUserId(Long userId);

    // Count user's recent transactions
    long countByUserIdAndTransactionTimeAfter(
            Long userId,
            LocalDateTime time
    );

    // Check whether the user has previous fraud transactions
    long countByUserIdAndStatus(
            Long userId,
            String status
    );

    // Check whether this receiver was previously used by the user
    boolean existsByUserIdAndReceiver(
            Long userId,
            String receiver
    );

    // Calculate user's previous average transaction amount
    @Query("""
            SELECT AVG(t.amount)
            FROM Transaction t
            WHERE t.userId = :userId
            """)
    Double findAverageAmountByUserId(
            @Param("userId") Long userId
    );
}