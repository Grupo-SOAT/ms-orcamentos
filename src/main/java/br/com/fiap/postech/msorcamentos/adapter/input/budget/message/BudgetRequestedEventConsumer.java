package br.com.fiap.postech.msorcamentos.adapter.input.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.EventDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetRequestedEventConsumer {

    private final EventDispatcher eventDispatcher;

    @KafkaListener(
            topics = "${app.events.topic.service-order}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "rawKafkaListenerContainerFactory"
    )
    public void consume(String payload, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        eventDispatcher.dispatch(payload);
    }
}
