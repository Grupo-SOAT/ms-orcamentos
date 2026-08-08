package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.time.Instant;

public record BudgetApprovalTokenView(
        Long serviceOrderId,
        String token,
        Instant expiresAt,
        Instant usedAt
) {

    public boolean isValid() {
        return usedAt == null && expiresAt.isAfter(Instant.now());
    }
}
