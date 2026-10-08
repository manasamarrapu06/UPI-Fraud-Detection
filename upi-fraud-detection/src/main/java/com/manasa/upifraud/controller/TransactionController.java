package com.manasa.upifraud.controller;

import com.manasa.upifraud.entity.Transaction;
import com.manasa.upifraud.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    // ==============================
    // CREATE TRANSACTION
    // ==============================

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody Transaction transaction,
            Authentication authentication) {

        // Check whether user is logged in
        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User session not found. Please login again.");
        }

        // Get logged-in user's email
        String email = authentication.getName();

        try {

            Transaction savedTransaction =
                    transactionService.saveTransaction(
                            transaction,
                            email
                    );

            return ResponseEntity.ok(savedTransaction);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    // ==============================
    // GET TRANSACTIONS
    // ==============================

    @GetMapping
    public ResponseEntity<?> getAllTransactions(
            Authentication authentication) {

        // Check login
        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User session not found. Please login again.");
        }

        // Check admin
        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        try {

            // ADMIN → all transactions
            if (isAdmin) {

                return ResponseEntity.ok(
                        transactionService.getAllTransactions()
                );
            }

            // USER → only own transactions
            String email = authentication.getName();

            List<Transaction> transactions =
                    transactionService.getTransactionsForUser(
                            email
                    );

            return ResponseEntity.ok(transactions);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}