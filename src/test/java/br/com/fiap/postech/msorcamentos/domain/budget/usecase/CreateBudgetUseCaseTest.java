package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetRequest;
import br.com.fiap.postech.msorcamentos.domain.budget.model.CatalogPrice;
import br.com.fiap.postech.msorcamentos.port.catalog.CatalogQueryPort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBudgetUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final CatalogQueryPort catalogQueryPort = mock(CatalogQueryPort.class);
    private final BudgetPersistencePort persistencePort = mock(BudgetPersistencePort.class);
    private final CreateBudgetUseCase useCase =
            new CreateBudgetUseCase(catalogQueryPort, persistencePort, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void should_price_items_with_supplies_and_persist() {
        when(catalogQueryPort.findPrice(1L)).thenReturn(new CatalogPrice(
                1L, BigDecimal.valueOf(100), Map.of(10L, BigDecimal.valueOf(5)), NOW));
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Budget budget = useCase.create(new BudgetRequest(7L, "BRL", List.of(
                new BudgetRequest.BudgetRequestItem(1L, 2,
                        List.of(new BudgetRequest.BudgetRequestSupply(10L, 3))))));

        assertThat(budget.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(215));
        assertThat(budget.getItems()).hasSize(1);
        assertThat(budget.getItems().get(0).priceFetchedAt()).isEqualTo(NOW);
        assertThat(budget.getItems().get(0).supplies().get(0).unitPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(5));
        verify(persistencePort).save(budget);
    }

    @Test
    void should_default_currency_to_brl() {
        when(catalogQueryPort.findPrice(1L)).thenReturn(new CatalogPrice(
                1L, BigDecimal.TEN, Map.of(), NOW));
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Budget budget = useCase.create(new BudgetRequest(7L, null, List.of(
                new BudgetRequest.BudgetRequestItem(1L, 1, List.of()))));

        assertThat(budget.getCurrency()).isEqualTo("BRL");
    }

    @Test
    void should_reject_missing_service_order() {
        assertThatThrownBy(() -> useCase.create(new BudgetRequest(null, "BRL", List.of(
                new BudgetRequest.BudgetRequestItem(1L, 1, List.of())))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_empty_items() {
        assertThatThrownBy(() -> useCase.create(new BudgetRequest(7L, "BRL", List.of())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_reject_missing_supply_price() {
        when(catalogQueryPort.findPrice(1L)).thenReturn(new CatalogPrice(
                1L, BigDecimal.TEN, Map.of(), NOW));

        assertThatThrownBy(() -> useCase.create(new BudgetRequest(7L, "BRL", List.of(
                new BudgetRequest.BudgetRequestItem(1L, 1,
                        List.of(new BudgetRequest.BudgetRequestSupply(404L, 1)))))))
                .isInstanceOf(MissingSupplyPriceException.class)
                .hasMessageContaining("404");
    }
}
