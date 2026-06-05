package com.servecreative.WholeProject.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paymentId;

    @OneToOne
    @JoinColumn(name = "duty_id", nullable = false, unique = true)
    private Duty duty;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private String transactionRef;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING, PAID, FAILED
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public Duty getDuty() { return duty; }
    public void setDuty(Duty duty) { this.duty = duty; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
