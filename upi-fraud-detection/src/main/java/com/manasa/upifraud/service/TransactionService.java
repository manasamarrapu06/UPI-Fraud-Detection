package com.manasa.upifraud.service;

import com.manasa.upifraud.entity.Transaction;
import com.manasa.upifraud.entity.User;
import com.manasa.upifraud.repository.TransactionRepository;
import com.manasa.upifraud.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AiService aiService;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            FraudDetectionService fraudDetectionService,
            AiService aiService,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    public Transaction saveTransaction(
            Transaction transaction,
            String email) {

        // Find the logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Logged-in user not found"));

        // Automatically connect transaction to the user
        transaction.setUserId(user.getId());

        // Automatically use the logged-in user's name as sender
        transaction.setSender(user.getFullName());

        // Rule-based fraud detection
        String ruleResult =
                fraudDetectionService.detectFraud(transaction);

        // AI-based fraud detection
        String aiResult =
                aiService.predictFraud(transaction);

        System.out.println(
                "Rule-Based Result: " + ruleResult);

        System.out.println(
                "AI Result: " + aiResult);

        boolean ruleSafe =
                "No fraud detected".equalsIgnoreCase(ruleResult)
                || "SAFE".equalsIgnoreCase(ruleResult);

        boolean aiSafe =
                "SAFE".equalsIgnoreCase(aiResult);

        if (ruleSafe && aiSafe) {

            transaction.setStatus("SAFE");
            transaction.setFraudReason(null);

        } else {

            transaction.setStatus("SUSPICIOUS");

            if (!aiSafe) {

                transaction.setFraudReason(
                        "AI model detected suspicious transaction"
                );

            } else {

                transaction.setFraudReason(ruleResult);
            }
        }

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsForUser(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return transactionRepository
                .findByUserId(user.getId());
    }

    // Used by Admin dashboard
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}