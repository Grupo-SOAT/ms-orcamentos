package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.math.BigDecimal;

public record ServiceOrderSummary(
        Long serviceOrderId,
        String description,
        BigDecimal estimatedAmount,
        String clientName,
        String clientEmail
) {
}
