package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.msorcamentos.port.email.BudgetApprovalEmailSenderPort;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;

public class SendBudgetApprovalEmailUseCase {

    private final ServiceOrderReadPort serviceOrderReadPort;
    private final BudgetApprovalEmailSenderPort budgetApprovalEmailSenderPort;

    public SendBudgetApprovalEmailUseCase(
            ServiceOrderReadPort serviceOrderReadPort,
            BudgetApprovalEmailSenderPort budgetApprovalEmailSenderPort
    ) {
        this.serviceOrderReadPort = serviceOrderReadPort;
        this.budgetApprovalEmailSenderPort = budgetApprovalEmailSenderPort;
    }

    public void send(Long serviceOrderId, String token) {
        var summary = serviceOrderReadPort.findSummaryById(serviceOrderId)
                .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        budgetApprovalEmailSenderPort.send(summary, token);
    }
}
