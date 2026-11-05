package br.com.fiap.postech.msorcamentos.adapter.input.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.BudgetRequestedEvent;
import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.EventTypes;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetCreatedEvent;
import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItem;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetRequest;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.CreateBudgetUseCase;
import br.com.fiap.postech.msorcamentos.port.message.BudgetEventPublisherPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BudgetRequestedHandlerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final CreateBudgetUseCase createBudgetUseCase = mock(CreateBudgetUseCase.class);
    private final BudgetEventPublisherPort publisher = mock(BudgetEventPublisherPort.class);
    private final BudgetRequestedHandler handler = new BudgetRequestedHandler(createBudgetUseCase, publisher);

    @Test
    void should_price_and_publish_budget_created() {
        BudgetRequest request = new BudgetRequest(7L, "BRL", List.of());
        Budget budget = Budget.create(7L, "BRL",
                List.of(new BudgetItem(1L, BigDecimal.TEN, 1, List.of(), NOW)), NOW);
        budget.assignId(99L);
        when(createBudgetUseCase.create(request)).thenReturn(budget);

        BudgetRequestedEvent event = new BudgetRequestedEvent();
        event.setEventId("evt-1");
        event.setType(EventTypes.BUDGET_REQUESTED);
        event.setCorrelationId("corr-1");
        event.setServiceOrderId(7L);
        event.setPayload(request);

        handler.handle(event);

        ArgumentCaptor<BudgetCreatedEvent> captor = ArgumentCaptor.forClass(BudgetCreatedEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(EventTypes.BUDGET_CREATED);
        assertThat(captor.getValue().correlationId()).isEqualTo("corr-1");
        assertThat(captor.getValue().serviceOrderId()).isEqualTo(7L);
        assertThat(captor.getValue().payload().budgetId()).isEqualTo(99L);
        assertThat(captor.getValue().payload().totalAmount()).isEqualByComparingTo(BigDecimal.TEN);
    }
}
