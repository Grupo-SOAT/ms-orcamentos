package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BudgetItem(
        Long catalogServiceId,
        BigDecimal unitPrice,
        int quantity,
        List<BudgetItemSupply> supplies,
        Instant priceFetchedAt
) {

    public BudgetItem {
        if (catalogServiceId == null) {
            throw new IllegalArgumentException("catalogServiceId is required");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        supplies = supplies == null ? List.of() : List.copyOf(supplies);
    }

    public BigDecimal lineTotal() {
        BigDecimal suppliesTotal = supplies.stream()
                .map(BudgetItemSupply::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).add(suppliesTotal);
    }
}
