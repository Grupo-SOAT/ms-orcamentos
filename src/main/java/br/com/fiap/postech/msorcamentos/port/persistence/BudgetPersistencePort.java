package br.com.fiap.postech.msorcamentos.port.persistence;

import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;

import java.util.Optional;

public interface BudgetPersistencePort {

    Budget save(Budget budget);

    Optional<Budget> findById(Long id);
}
