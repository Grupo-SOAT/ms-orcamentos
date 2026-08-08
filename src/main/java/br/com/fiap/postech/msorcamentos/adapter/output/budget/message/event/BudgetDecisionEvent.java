package br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event;

public record BudgetDecisionEvent(Long serviceOrderId, String decision) {
}
