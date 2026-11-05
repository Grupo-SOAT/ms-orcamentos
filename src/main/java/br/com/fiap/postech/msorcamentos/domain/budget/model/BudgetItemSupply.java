package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.math.BigDecimal;
import java.time.Instant;

public record BudgetItemSupply(
        Long supplyId,
        BigDecimal unitPrice,
        int quantity,
        Instant priceFetchedAt
) {

    public BudgetItemSupply {
        if (supplyId == null) {
            throw new IllegalArgumentException("supplyId is required");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
