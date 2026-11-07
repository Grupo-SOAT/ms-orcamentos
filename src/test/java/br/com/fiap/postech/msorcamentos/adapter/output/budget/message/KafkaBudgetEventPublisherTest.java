package br.com.fiap.postech.msorcamentos.adapter.output.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaBudgetEventPublisherTest {

    @Test
    void should_send_budget_created_with_service_order_key() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaBudgetEventPublisher publisher = new KafkaBudgetEventPublisher(kafkaTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(publisher, "topic", "budget-events");
        BudgetCreatedEvent event = new BudgetCreatedEvent("evt-1", "BUDGET_CREATED", Instant.now(),
                "corr-1", 7L, new BudgetCreatedEvent.Payload(99L, BigDecimal.TEN, "BRL"));

        publisher.publish(event);

        verify(kafkaTemplate).send("budget-events", "7", event);
    }
}
