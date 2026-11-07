package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Budget aggregate. Owns the priced items and the total amount computed from catalog prices.
 */
public class Budget {

    private Long id;
    private final Long serviceOrderId;
    private final String currency;
    private final List<BudgetItem> items;
    private final BigDecimal totalAmount;
    private final Instant createdAt;
    private Instant sentAt;
    private Status status;

    private Budget(Long id, Long serviceOrderId, String currency, List<BudgetItem> items,
                   BigDecimal totalAmount, Instant createdAt, Instant sentAt, Status status) {
        this.id = id;
        this.serviceOrderId = serviceOrderId;
        this.currency = currency;
        this.items = items;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.status = status;
    }

    public static Budget create(Long serviceOrderId, String currency, List<BudgetItem> items, Instant now) {
        Objects.requireNonNull(serviceOrderId, "serviceOrderId is required");
        Objects.requireNonNull(currency, "currency is required");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("budget must have at least one item");
        }
        BigDecimal total = items.stream()
                .map(BudgetItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Budget(null, serviceOrderId, currency, List.copyOf(items), total, now, null, Status.DRAFT);
    }

    public static Budget restore(Long id, Long serviceOrderId, String currency, List<BudgetItem> items,
                                 BigDecimal totalAmount, Instant createdAt, Instant sentAt, Status status) {
        return new Budget(id, serviceOrderId, currency, items, totalAmount, createdAt, sentAt, status);
    }

    public void markSent(Instant now) {
        this.sentAt = now;
        this.status = Status.SENT;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getServiceOrderId() {
        return serviceOrderId;
    }

    public String getCurrency() {
        return currency;
    }

    public List<BudgetItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        DRAFT,
        SENT,
        APPROVED,
        REJECTED,
        CANCELLED
    }
}
