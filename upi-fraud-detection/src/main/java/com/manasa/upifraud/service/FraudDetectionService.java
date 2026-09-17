package com.manasa.upifraud.service;

import org.springframework.stereotype.Service;

import com.manasa.upifraud.entity.Transaction;

@Service
public class FraudDetectionService {

    public String detectFraud(Transaction transaction) {

        double amount = transaction.getAmount();
        String location = transaction.getLocation();

        double riskScore = 0.0;
        StringBuilder reasons = new StringBuilder();

        // ---------------------------------------------
        // RULE 1: Invalid amount
        // ---------------------------------------------
        if (amount <= 0) {

            transaction.setRiskScore(100.0);
            transaction.setRiskLevel("HIGH");

            return "Invalid transaction amount";
        }

        // ---------------------------------------------
        // RULE 2: Transaction amount
        // ---------------------------------------------
        if (amount > 100000) {

            riskScore += 60;
            reasons.append("Very high transaction amount; ");

        } else if (amount > 50000) {

            riskScore += 40;
            reasons.append("High transaction amount; ");

        } else if (amount > 25000) {

            riskScore += 20;
            reasons.append("Unusually large transaction; ");
        }

        // ---------------------------------------------
        // RULE 3: New receiver
        // ---------------------------------------------
        if (transaction.isNewReceiver()) {

            riskScore += 15;
            reasons.append("New receiver; ");
        }

        // ---------------------------------------------
        // RULE 4: Device changed
        // ---------------------------------------------
        if (transaction.isDeviceChanged()) {

            riskScore += 20;
            reasons.append("New device detected; ");
        }

        // ---------------------------------------------
        // RULE 5: Location changed
        // ---------------------------------------------
        if (transaction.isLocationChanged()) {

            riskScore += 15;
            reasons.append("Unusual location detected; ");
        }

        // ---------------------------------------------
        // RULE 6: Transaction frequency
        // ---------------------------------------------
        if (transaction.getTransactionsLastHour() >= 10) {

            riskScore += 20;
            reasons.append("High transaction frequency; ");

        } else if (transaction.getTransactionsLastHour() >= 5) {

            riskScore += 10;
            reasons.append("Multiple transactions in short time; ");
        }

        // ---------------------------------------------
        // RULE 7: Previous average amount
        // ---------------------------------------------
        double previousAverage =
                transaction.getPreviousAverageAmount();

        if (previousAverage > 0 &&
                amount > previousAverage * 5) {

            riskScore += 20;
            reasons.append("Amount is much higher than usual; ");
        }

        // ---------------------------------------------
        // RULE 8: Failed attempts
        // ---------------------------------------------
        if (transaction.getFailedAttempts() >= 5) {

            riskScore += 20;
            reasons.append("Multiple failed attempts; ");

        } else if (transaction.getFailedAttempts() >= 3) {

            riskScore += 10;
            reasons.append("Repeated failed attempts; ");
        }

        // ---------------------------------------------
        // RULE 9: Location missing
        // ---------------------------------------------
        if (location == null ||
                location.trim().isEmpty()) {

            riskScore += 10;
            reasons.append("Location information missing; ");
        }

        // ---------------------------------------------
        // Maximum risk score = 100
        // ---------------------------------------------
        if (riskScore > 100) {
            riskScore = 100;
        }

        // Save risk score
        transaction.setRiskScore(riskScore);

        // ---------------------------------------------
        // Risk level
        // ---------------------------------------------
        if (riskScore >= 70) {

            transaction.setRiskLevel("HIGH");

        } else if (riskScore >= 40) {

            transaction.setRiskLevel("MEDIUM");

        } else {

            transaction.setRiskLevel("LOW");
        }

        // ---------------------------------------------
        // Return fraud reason
        // ---------------------------------------------
        if (reasons.length() == 0) {

            return "No fraud detected";
        }

        return reasons.toString();
    }
}