package com.manasa.upifraud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Logged-in user ID
    private Long userId;

    // Basic transaction details
    private String sender;

    @NotBlank(message = "Receiver is required")
    private String receiver;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    @NotBlank(message = "Location is required")
    private String location;

    // Transaction result
    private String status;
    private String fraudReason;

    // AI/ML fraud detection features
    private LocalDateTime transactionTime;
    private boolean newReceiver;
    private boolean deviceChanged;
    private boolean locationChanged;
    private int transactionsLastHour;
    private double previousAverageAmount;
    private int failedAttempts;

    // AI prediction result
    private double riskScore;
    private String riskLevel;

    public Transaction() {
        this.transactionTime = LocalDateTime.now();
        this.riskLevel = "UNKNOWN";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public boolean isNewReceiver() {
        return newReceiver;
    }

    public void setNewReceiver(boolean newReceiver) {
        this.newReceiver = newReceiver;
    }

    public boolean isDeviceChanged() {
        return deviceChanged;
    }

    public void setDeviceChanged(boolean deviceChanged) {
        this.deviceChanged = deviceChanged;
    }

    public boolean isLocationChanged() {
        return locationChanged;
    }

    public void setLocationChanged(boolean locationChanged) {
        this.locationChanged = locationChanged;
    }

    public int getTransactionsLastHour() {
        return transactionsLastHour;
    }

    public void setTransactionsLastHour(int transactionsLastHour) {
        this.transactionsLastHour = transactionsLastHour;
    }

    public double getPreviousAverageAmount() {
        return previousAverageAmount;
    }

    public void setPreviousAverageAmount(double previousAverageAmount) {
        this.previousAverageAmount = previousAverageAmount;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}