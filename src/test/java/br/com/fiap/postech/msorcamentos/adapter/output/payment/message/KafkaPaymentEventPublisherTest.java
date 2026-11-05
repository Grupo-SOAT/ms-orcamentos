package br.com.fiap.postech.msorcamentos.adapter.output.payment.message;

import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaPaymentEventPublisherTest {

    @Test
    void should_publish_with_service_order_key() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaPaymentEventPublisher publisher = new KafkaPaymentEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "topic", "payment-events");
        PaymentEvent event = new PaymentEvent("evt-1", PaymentEvent.PAYMENT_APPROVED, Instant.now(),
                "corr-1", 7L, "mp-1");

        publisher.publish(event);

        verify(kafkaTemplate).send("payment-events", "7", event);
    }
}
