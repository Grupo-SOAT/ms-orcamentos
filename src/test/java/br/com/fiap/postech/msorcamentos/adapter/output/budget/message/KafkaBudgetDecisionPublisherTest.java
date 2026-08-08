package br.com.fiap.postech.msorcamentos.adapter.output.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetDecisionEvent;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class KafkaBudgetDecisionPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaBudgetDecisionPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaBudgetDecisionPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "topic", "budget-decision");
    }

    @Test
    void should_publish_event_with_service_order_id_as_key() {
        publisher.publish(1L, BudgetDecision.APPROVE);

        verify(kafkaTemplate).send("budget-decision", "1", new BudgetDecisionEvent(1L, "APPROVE"));
    }
}
