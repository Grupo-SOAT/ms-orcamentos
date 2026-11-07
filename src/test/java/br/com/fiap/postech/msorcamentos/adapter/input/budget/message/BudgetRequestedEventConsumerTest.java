package br.com.fiap.postech.msorcamentos.adapter.input.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.EventDispatcher;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BudgetRequestedEventConsumerTest {

    private final EventDispatcher dispatcher = mock(EventDispatcher.class);
    private final BudgetRequestedEventConsumer consumer = new BudgetRequestedEventConsumer(dispatcher);

    @Test
    void should_dispatch_payload() {
        consumer.consume("{\"type\":\"BUDGET_REQUESTED\"}", "7");

        verify(dispatcher).dispatch("{\"type\":\"BUDGET_REQUESTED\"}");
    }
}
