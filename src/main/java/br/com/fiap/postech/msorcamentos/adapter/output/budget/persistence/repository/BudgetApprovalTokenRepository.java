package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.repository;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetApprovalTokenEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Extends o marker interface {@link Repository} (nao {@code JpaRepository}) de proposito:
 * assim nenhum metodo de escrita (save/delete) fica exposto neste servico somente-leitura.
 */
public interface BudgetApprovalTokenRepository extends Repository<BudgetApprovalTokenEntity, Long> {

    Optional<BudgetApprovalTokenEntity> findByServiceOrderIdAndToken(Long serviceOrderId, String token);
}
