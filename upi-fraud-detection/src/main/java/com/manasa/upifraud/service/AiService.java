package com.manasa.upifraud.service;

import com.manasa.upifraud.entity.Transaction;
import com.manasa.upifraud.repository.TransactionRepository;
import com.manasa.upifraud.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AiService(
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.restClient = RestClient.builder()
        .baseUrl("https://upi-fraud-detection-js85.vercel.app")
        .build();

        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public String predictFraud(Transaction transaction) {

        double amount = transaction.getAmount();

        int locationRisk =
                calculateLocationRisk(transaction.getLocation());

        /*
         * Calculate transaction frequency
         *
         * Count transactions made by this user
         * during the last one hour.
         */
        int transactionFrequency = 1;

        if (transaction.getUserId() != null) {

            LocalDateTime oneHourAgo =
                    LocalDateTime.now().minusHours(1);

            long count =
                    transactionRepository
                            .countByUserIdAndTransactionTimeAfter(
                                    transaction.getUserId(),
                                    oneHourAgo
                            );

            transactionFrequency = (int) count + 1;
        }

        /*
         * Check previous fraud history.
         */
        int previousFraud = 0;

        if (transaction.getUserId() != null) {

            long fraudCount =
                    transactionRepository
                            .countByUserIdAndStatus(
                                    transaction.getUserId(),
                                    "SUSPICIOUS"
                            );

            if (fraudCount > 0) {
                previousFraud = 1;
            }
        }

        /*
         * Check whether the receiver is new.
         */
        int recipientNew = 1;

        if (transaction.getUserId() != null
                && transaction.getReceiver() != null) {

            boolean receiverExists =
                    transactionRepository
                            .existsByUserIdAndReceiver(
                                    transaction.getUserId(),
                                    transaction.getReceiver()
                            );

            if (receiverExists) {
                recipientNew = 0;
            }
        }

        /*
         * Check whether transaction happens at night.
         *
         * Night period:
         * 10 PM to 6 AM
         */
        int nightTransaction = 0;

        int hour = LocalDateTime.now().getHour();

        if (hour >= 22 || hour < 6) {
            nightTransaction = 1;
        }

        /*
         * Get user's previous average transaction amount.
         */
        double previousAverageAmount = 0;

        if (transaction.getUserId() != null) {

            Double average =
                    transactionRepository
                            .findAverageAmountByUserId(
                                    transaction.getUserId()
                            );

            if (average != null) {
                previousAverageAmount = average;
            }
        }

        /*
         * Store calculated values in Transaction.
         */
        transaction.setTransactionsLastHour(
                transactionFrequency
        );

        transaction.setPreviousAverageAmount(
                previousAverageAmount
        );

        transaction.setNewReceiver(
                recipientNew == 1
        );

        /*
         * Prepare data for Python AI service.
         */
        Map<String, Object> request = Map.of(

                "amount", amount,

                "location_risk", locationRisk,

                "transaction_frequency",
                transactionFrequency,

                "previous_fraud",
                previousFraud,

                "recipient_new",
                recipientNew,

                "night_transaction",
                nightTransaction
        );

        try {

            Map response =
                    restClient.post()
                            .uri("/predict")
                            .body(request)
                            .retrieve()
                            .body(Map.class);

            if (response != null) {

                Object resultObject =
                        response.get("result");

                Object riskScoreObject =
                        response.get("risk_score");

                double riskScore = 0;

                if (riskScoreObject != null) {

                    riskScore =
                            Double.parseDouble(
                                    riskScoreObject.toString()
                            );
                }

                /*
                 * Very large transactions are treated
                 * as high risk.
                 */
                if (amount >= 1000000) {

                    riskScore =
                            Math.max(riskScore, 90);

                    transaction.setRiskLevel("HIGH");

                } else if (riskScore >= 70) {

                    transaction.setRiskLevel("HIGH");

                } else if (riskScore >= 40) {

                    transaction.setRiskLevel("MEDIUM");

                } else {

                    transaction.setRiskLevel("LOW");
                }

                transaction.setRiskScore(riskScore);

                if (resultObject != null) {
                    return resultObject.toString();
                }
            }

            transaction.setRiskLevel("UNKNOWN");

            return "UNKNOWN";

        } catch (Exception e) {

            System.out.println(
                    "AI Service Error: "
                            + e.getMessage()
            );

            transaction.setRiskLevel("UNKNOWN");

            return "UNKNOWN";
        }
    }

    private int calculateLocationRisk(String location) {

        if (location == null
                || location.trim().isEmpty()) {

            return 1;
        }

        String city =
                location.trim().toLowerCase();

        if (city.equals("hyderabad")
                || city.equals("visakhapatnam")
                || city.equals("vijayawada")) {

            return 0;
        }

        return 1;
    }
}
