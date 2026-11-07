package br.com.fiap.postech.msorcamentos.domain.payment.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Payment aggregate for a budget. One preference is reused while pending; the decision can only
 * be consumed once.
 */
public class Payment {

    private Long id;
    private final Long serviceOrderId;
    private final Long budgetId;
    private String preferenceId;
    private String paymentId;
    private PaymentStatus status;
    private final BigDecimal amount;
    private String initPoint;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant usedAt;
    private Instant decidedAt;

    private Payment(Long id, Long serviceOrderId, Long budgetId, String preferenceId, String paymentId,
                    PaymentStatus status, BigDecimal amount, String initPoint, Instant createdAt,
                    Instant expiresAt, Instant usedAt, Instant decidedAt) {
        this.id = id;
        this.serviceOrderId = serviceOrderId;
        this.budgetId = budgetId;
        this.preferenceId = preferenceId;
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
        this.initPoint = initPoint;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.decidedAt = decidedAt;
    }

    public static Payment createPreference(Long serviceOrderId, Long budgetId, String preferenceId,
                                           String initPoint, BigDecimal amount, Instant now,
                                           Duration ttl) {
        Objects.requireNonNull(serviceOrderId, "serviceOrderId is required");
        Objects.requireNonNull(budgetId, "budgetId is required");
        Objects.requireNonNull(preferenceId, "preferenceId is required");
        Objects.requireNonNull(amount, "amount is required");
        return new Payment(null, serviceOrderId, budgetId, preferenceId, null, PaymentStatus.PENDING,
                amount, initPoint, now, now.plus(ttl), null, null);
    }

    public static Payment restore(Long id, Long serviceOrderId, Long budgetId, String preferenceId,
                                  String paymentId, PaymentStatus status, BigDecimal amount, String initPoint,
                                  Instant createdAt, Instant expiresAt, Instant usedAt, Instant decidedAt) {
        return new Payment(id, serviceOrderId, budgetId, preferenceId, paymentId, status, amount,
                initPoint, createdAt, expiresAt, usedAt, decidedAt);
    }

    public boolean isActive(Instant now) {
        return status == PaymentStatus.PENDING && now.isBefore(expiresAt);
    }

    public void approve(String externalPaymentId, Instant now) {
        requirePending();
        this.paymentId = externalPaymentId;
        this.status = PaymentStatus.APPROVED;
        this.decidedAt = now;
    }

    public void reject(String externalPaymentId, Instant now) {
        requirePending();
        this.paymentId = externalPaymentId;
        this.status = PaymentStatus.REJECTED;
        this.decidedAt = now;
    }

    public void expire(Instant now) {
        requirePending();
        this.status = PaymentStatus.EXPIRED;
        this.decidedAt = now;
    }

    public void markRefunded(Instant now) {
        if (status != PaymentStatus.APPROVED) {
            throw new IllegalStateException("Only approved payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
        this.decidedAt = now;
    }

    public void markDecisionUsed(Instant now) {
        if (usedAt != null) {
            throw new PaymentDecisionAlreadyUsedException(serviceOrderId);
        }
        this.usedAt = now;
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentTransitionException(status);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getServiceOrderId() {
        return serviceOrderId;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
