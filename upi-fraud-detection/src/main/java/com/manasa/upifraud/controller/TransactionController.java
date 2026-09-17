package com.manasa.upifraud.controller;

import com.manasa.upifraud.entity.Transaction;
import com.manasa.upifraud.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Create and analyze a transaction
    @PostMapping
    public Transaction createTransaction(
            @Valid @RequestBody Transaction transaction,
            Authentication authentication) {

        // Get email of logged-in user
        String email = authentication.getName();

        // Save transaction for that user
        return transactionService.saveTransaction(
                transaction,
                email
        );
    }

    // Get transactions of logged-in user
    // Admin can see all transactions
    @GetMapping
    public List<Transaction> getAllTransactions(
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));

        // Admin → all transactions
        if (isAdmin) {
            return transactionService.getAllTransactions();
        }

        // User → only their own transactions
        String email = authentication.getName();

        return transactionService.getTransactionsForUser(
                email
        );
    }
}