package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.repository.BudgetApprovalTokenRepository;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetApprovalTokenView;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetApprovalTokenReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BudgetApprovalTokenReadAdapter implements BudgetApprovalTokenReadPort {

    private final BudgetApprovalTokenRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<BudgetApprovalTokenView> findToken(Long serviceOrderId, String token) {
        return repository.findByServiceOrderIdAndToken(serviceOrderId, token)
                .map(entity -> new BudgetApprovalTokenView(
                        entity.getServiceOrderId(),
                        entity.getToken(),
                        entity.getExpiresAt(),
                        entity.getUsedAt()
                ));
    }
}
