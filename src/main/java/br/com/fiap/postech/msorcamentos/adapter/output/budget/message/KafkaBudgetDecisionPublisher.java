package br.com.fiap.postech.msorcamentos.adapter.output.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetDecisionEvent;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;
import br.com.fiap.postech.msorcamentos.port.message.BudgetDecisionPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaBudgetDecisionPublisher implements BudgetDecisionPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.budget.kafka.topic.decision}")
    private String topic;

    @Override
    public void publish(Long serviceOrderId, BudgetDecision decision) {
        var event = new BudgetDecisionEvent(serviceOrderId, decision.name());
        kafkaTemplate.send(topic, serviceOrderId.toString(), event);
    }
}
