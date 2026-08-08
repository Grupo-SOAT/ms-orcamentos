package br.com.fiap.postech.msorcamentos.adapter.input.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.BudgetApprovalRequestEvent;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.SendBudgetApprovalEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetApprovalRequestKafkaConsumer {

    private final SendBudgetApprovalEmailUseCase sendBudgetApprovalEmailUseCase;

    @KafkaListener(topics = "${app.budget.kafka.topic.request}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(
            BudgetApprovalRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        if (key != null && !key.equals(String.valueOf(event.getServiceOrderId()))) {
            log.warn("Kafka key mismatch: received key={} but event.serviceOrderId={}", key, event.getServiceOrderId());
        }
        sendBudgetApprovalEmailUseCase.send(event.getServiceOrderId(), event.getToken());
    }
}
