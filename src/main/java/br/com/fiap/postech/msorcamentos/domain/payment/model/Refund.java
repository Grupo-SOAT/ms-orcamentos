package br.com.fiap.postech.msorcamentos.domain.payment.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Refund aggregate. The payment id is unique, so a second request is recognised as a duplicate.
 */
public class Refund {

    private Long id;
    private final Long paymentId;
    private final Long serviceOrderId;
    private final BigDecimal amount;
    private final String status;
    private final Instant createdAt;

    private Refund(Long id, Long paymentId, Long serviceOrderId, BigDecimal amount, String status,
                   Instant createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.serviceOrderId = serviceOrderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Refund create(Long paymentId, Long serviceOrderId, BigDecimal amount, Instant now) {
        Objects.requireNonNull(paymentId, "paymentId is required");
        Objects.requireNonNull(serviceOrderId, "serviceOrderId is required");
        Objects.requireNonNull(amount, "amount is required");
        return new Refund(null, paymentId, serviceOrderId, amount, "APPROVED", now);
    }

    public static Refund restore(Long id, Long paymentId, Long serviceOrderId, BigDecimal amount,
                                 String status, Instant createdAt) {
        return new Refund(id, paymentId, serviceOrderId, amount, status, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getServiceOrderId() {
        return serviceOrderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
