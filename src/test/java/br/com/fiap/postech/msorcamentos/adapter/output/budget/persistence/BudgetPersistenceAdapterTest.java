package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.repository.BudgetRepository;
import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItem;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItemSupply;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BudgetPersistenceAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final BudgetRepository repository = mock(BudgetRepository.class);
    private final BudgetPersistenceAdapter adapter = new BudgetPersistenceAdapter(repository);

    @Test
    void should_persist_and_restore_the_full_budget_audit() {
        BudgetItem item = new BudgetItem(1L, BigDecimal.valueOf(100), 2,
                List.of(new BudgetItemSupply(10L, BigDecimal.valueOf(5), 3, NOW)), NOW);
        Budget budget = Budget.create(7L, "BRL", List.of(item), NOW);

        when(repository.save(any(BudgetEntity.class))).thenAnswer(invocation -> {
            BudgetEntity entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        Budget saved = adapter.save(budget);

        assertThat(saved).usingRecursiveComparison()
                .isEqualTo(Budget.restore(99L, 7L, "BRL", List.of(item),
                        BigDecimal.valueOf(215), NOW, null, Budget.Status.DRAFT));
        assertThat(saved.getItems().get(0).supplies().get(0).priceFetchedAt()).isEqualTo(NOW);
    }
}
