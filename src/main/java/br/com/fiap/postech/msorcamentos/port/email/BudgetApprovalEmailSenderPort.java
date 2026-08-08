package br.com.fiap.postech.msorcamentos.port.email;

import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;

public interface BudgetApprovalEmailSenderPort {

    void send(ServiceOrderSummary summary, String token);
}
