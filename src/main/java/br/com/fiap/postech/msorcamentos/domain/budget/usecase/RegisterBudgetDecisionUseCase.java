package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidOrExpiredBudgetTokenException;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;
import br.com.fiap.postech.msorcamentos.port.message.BudgetDecisionPublisherPort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetApprovalTokenReadPort;

public class RegisterBudgetDecisionUseCase {

    private final BudgetApprovalTokenReadPort budgetApprovalTokenReadPort;
    private final BudgetDecisionPublisherPort budgetDecisionPublisherPort;

    public RegisterBudgetDecisionUseCase(
            BudgetApprovalTokenReadPort budgetApprovalTokenReadPort,
            BudgetDecisionPublisherPort budgetDecisionPublisherPort
    ) {
        this.budgetApprovalTokenReadPort = budgetApprovalTokenReadPort;
        this.budgetDecisionPublisherPort = budgetDecisionPublisherPort;
    }

    public void process(Long serviceOrderId, String token, BudgetDecision decision) {
        validateToken(serviceOrderId, token);
        budgetDecisionPublisherPort.publish(serviceOrderId, decision);
    }

    public void validateToken(Long serviceOrderId, String token) {
        var tokenView = budgetApprovalTokenReadPort.findToken(serviceOrderId, token)
                .orElseThrow(() -> new InvalidOrExpiredBudgetTokenException(serviceOrderId));

        if (!tokenView.isValid()) {
            throw new InvalidOrExpiredBudgetTokenException(serviceOrderId);
        }
    }
}
