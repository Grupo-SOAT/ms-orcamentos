package br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event;

import br.com.fiap.postech.msorcamentos.port.monitoring.EventMetricsPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Dispatches deserialized events to registered handlers.
 *
 * <p>Events with an unregistered type arrive as {@link UnknownEvent}. They are acknowledged,
 * logged at INFO and counted; they never go to a dead-letter topic because multiplexed topics
 * legitimately carry event types this service does not consume.</p>
 */
@Slf4j
public class EventDispatcher {

    private final ObjectMapper objectMapper;
    private final String topic;
    private final EventMetricsPort metrics;
    private final Map<Class<?>, Consumer<EventEnvelope>> handlers;

    public EventDispatcher(
            ObjectMapper objectMapper,
            String topic,
            EventMetricsPort metrics,
            Map<Class<?>, Consumer<EventEnvelope>> handlers
    ) {
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.metrics = metrics;
        this.handlers = Map.copyOf(handlers);
    }

    public void dispatch(String payload) {
        EventEnvelope event;
        try {
            event = objectMapper.readValue(payload, EventEnvelope.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Cannot deserialize event", exception);
        }

        Consumer<EventEnvelope> handler = handlers.get(event.getClass());
        if (handler == null) {
            metrics.recordIgnored(topic, event.getType());
            log.info("Ignoring unregistered event type={} eventId={}", event.getType(), event.getEventId());
            return;
        }

        metrics.recordConsumed(topic, event.getType());
        handler.accept(event);
    }
}
