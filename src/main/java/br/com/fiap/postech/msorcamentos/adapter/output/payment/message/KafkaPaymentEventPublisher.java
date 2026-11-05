package br.com.fiap.postech.msorcamentos.adapter.output.payment.message;

import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.events.topic.payment}")
    private String topic;

    @Override
    public void publish(PaymentEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.serviceOrderId()), event);
    }
}
