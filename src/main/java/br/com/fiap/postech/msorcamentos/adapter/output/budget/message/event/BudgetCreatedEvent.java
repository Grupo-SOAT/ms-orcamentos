package br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.EventTypes;
import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetCreatedEvent(
        String eventId,
        String type,
        Instant occurredAt,
        String correlationId,
        Long serviceOrderId,
        Payload payload
) {

    public static BudgetCreatedEvent of(Budget budget, String correlationId, Instant now) {
        return new BudgetCreatedEvent(
                UUID.randomUUID().toString(),
                EventTypes.BUDGET_CREATED,
                now,
                correlationId,
                budget.getServiceOrderId(),
                new Payload(budget.getId(), budget.getTotalAmount(), budget.getCurrency())
        );
    }

    public record Payload(Long budgetId, BigDecimal totalAmount, String currency) {
    }
}
