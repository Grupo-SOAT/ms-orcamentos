package br.com.fiap.postech.msorcamentos.port.persistence;

import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetApprovalTokenView;

import java.util.Optional;

public interface BudgetApprovalTokenReadPort {

    Optional<BudgetApprovalTokenView> findToken(Long serviceOrderId, String token);
}
