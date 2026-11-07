package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.repository;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
}
