package br.com.fiap.postech.msorcamentos.port.message;

import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;

public interface BudgetDecisionPublisherPort {

    void publish(Long serviceOrderId, BudgetDecision decision);
}
