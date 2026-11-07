package br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event;

import br.com.fiap.postech.msorcamentos.port.monitoring.EventMetricsPort;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EventDispatcherTest {

    private final EventMetricsPort metrics = mock(EventMetricsPort.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Test
    void should_deserialize_known_event_and_dispatch_to_handler() {
        AtomicReference<EventEnvelope> received = new AtomicReference<>();
        EventDispatcher dispatcher = new EventDispatcher(objectMapper, "budget-events", metrics,
                Map.of(BudgetRequestedEvent.class, received::set));

        dispatcher.dispatch("""
                {
                  "eventId": "evt-1",
                  "type": "BUDGET_REQUESTED",
                  "occurredAt": "2026-01-01T10:00:00Z",
                  "correlationId": "corr-1",
                  "serviceOrderId": 7,
                  "payload": {
                    "serviceOrderId": 7,
                    "currency": "BRL",
                    "items": [
                      {"catalogServiceId": 1, "quantity": 2,
                       "supplies": [{"supplyId": 10, "quantity": 3}],
                       "unknownFutureField": "ignored"}
                    ]
                  }
                }
                """);

        assertThat(received.get()).isInstanceOf(BudgetRequestedEvent.class);
        BudgetRequestedEvent event = (BudgetRequestedEvent) received.get();
        assertThat(event.getType()).isEqualTo(EventTypes.BUDGET_REQUESTED);
        assertThat(event.getCorrelationId()).isEqualTo("corr-1");
        assertThat(event.getPayload().items()).hasSize(1);
        assertThat(event.getPayload().items().get(0).catalogServiceId()).isEqualTo(1L);
        verify(metrics).recordConsumed("budget-events", EventTypes.BUDGET_REQUESTED);
    }

    @Test
    void should_acknowledge_unknown_event_type() {
        EventDispatcher dispatcher = new EventDispatcher(objectMapper, "budget-events", metrics, Map.of());

        dispatcher.dispatch("""
                {"eventId":"evt-2","type":"SOMETHING_ELSE","occurredAt":"2026-01-01T10:00:00Z"}
                """);

        verify(metrics).recordIgnored("budget-events", "SOMETHING_ELSE");
        verify(metrics, never()).recordConsumed("budget-events", "SOMETHING_ELSE");
    }

    @Test
    void should_reject_malformed_payload() {
        EventDispatcher dispatcher = new EventDispatcher(objectMapper, "budget-events", metrics, Map.of());

        assertThatThrownBy(() -> dispatcher.dispatch("not-json"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_keep_payload_values() {
        BudgetRequestedEvent event = objectMapper.convertValue(Map.of(
                "eventId", "evt-3",
                "type", EventTypes.BUDGET_REQUESTED,
                "serviceOrderId", 7,
                "payload", Map.of(
                        "serviceOrderId", 7,
                        "currency", "BRL",
                        "items", java.util.List.of(Map.of(
                                "catalogServiceId", 1,
                                "quantity", 1,
                                "supplies", java.util.List.of())))), BudgetRequestedEvent.class);

        assertThat(event.getPayload().items().get(0).quantity()).isEqualTo(1);
        assertThat(BigDecimal.ONE).isEqualByComparingTo(BigDecimal.ONE);
    }
}
