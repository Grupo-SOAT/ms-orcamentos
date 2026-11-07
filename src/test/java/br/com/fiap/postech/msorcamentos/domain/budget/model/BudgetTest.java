package br.com.fiap.postech.msorcamentos.domain.budget.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BudgetTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void should_compute_total_from_services_and_supplies() {
        BudgetItem item = new BudgetItem(1L, BigDecimal.valueOf(100), 2,
                List.of(new BudgetItemSupply(10L, BigDecimal.valueOf(5), 3, NOW)), NOW);

        Budget budget = Budget.create(7L, "BRL", List.of(item), NOW);

        assertThat(budget.getServiceOrderId()).isEqualTo(7L);
        assertThat(budget.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(215));
        assertThat(budget.getStatus()).isEqualTo(Budget.Status.DRAFT);
        assertThat(budget.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void should_reject_empty_items() {
        assertThatThrownBy(() -> Budget.create(7L, "BRL", List.of(), NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Budget.create(7L, "BRL", null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_missing_service_order_and_currency() {
        BudgetItem item = new BudgetItem(1L, BigDecimal.ONE, 1, List.of(), NOW);
        assertThatThrownBy(() -> Budget.create(null, "BRL", List.of(item), NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Budget.create(7L, null, List.of(item), NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_mark_budget_as_sent() {
        BudgetItem item = new BudgetItem(1L, BigDecimal.ONE, 1, List.of(), NOW);
        Budget budget = Budget.create(7L, "BRL", List.of(item), NOW);

        budget.markSent(NOW);

        assertThat(budget.getStatus()).isEqualTo(Budget.Status.SENT);
        assertThat(budget.getSentAt()).isEqualTo(NOW);
    }

    @Test
    void should_validate_item_and_supply() {
        assertThatThrownBy(() -> new BudgetItem(null, BigDecimal.ONE, 1, List.of(), NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BudgetItem(1L, null, 1, List.of(), NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BudgetItem(1L, BigDecimal.ONE, 0, List.of(), NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BudgetItemSupply(null, BigDecimal.ONE, 1, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BudgetItemSupply(1L, null, 1, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BudgetItemSupply(1L, BigDecimal.ONE, 0, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_restore_and_assign_id() {
        BudgetItem item = new BudgetItem(1L, BigDecimal.ONE, 1, List.of(), NOW);
        Budget budget = Budget.restore(9L, 7L, "BRL", List.of(item), BigDecimal.ONE, NOW, NOW, Budget.Status.SENT);

        assertThat(budget.getId()).isEqualTo(9L);
        assertThat(budget.getStatus()).isEqualTo(Budget.Status.SENT);
        assertThat(budget.getSentAt()).isEqualTo(NOW);

        budget.assignId(10L);
        assertThat(budget.getId()).isEqualTo(10L);
    }
}
