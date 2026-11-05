package br.com.fiap.postech.msorcamentos.adapter.output.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetCreatedEvent;
import br.com.fiap.postech.msorcamentos.port.message.BudgetEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaBudgetEventPublisher implements BudgetEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.events.topic.budget}")
    private String topic;

    @Override
    public void publish(BudgetCreatedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.serviceOrderId()), event);
    }
}
