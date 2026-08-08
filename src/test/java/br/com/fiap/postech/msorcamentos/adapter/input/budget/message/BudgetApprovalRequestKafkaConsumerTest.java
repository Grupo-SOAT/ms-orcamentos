package br.com.fiap.postech.msorcamentos.adapter.input.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.BudgetApprovalRequestEvent;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.SendBudgetApprovalEmailUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class BudgetApprovalRequestKafkaConsumerTest {

    @Mock
    private SendBudgetApprovalEmailUseCase sendBudgetApprovalEmailUseCase;

    @InjectMocks
    private BudgetApprovalRequestKafkaConsumer consumer;

    @Test
    void should_delegate_to_use_case_when_key_matches() {
        var event = new BudgetApprovalRequestEvent();
        event.setServiceOrderId(1L);
        event.setToken("some-token");

        consumer.consume(event, "1");

        verify(sendBudgetApprovalEmailUseCase).send(1L, "some-token");
    }

    @Test
    void should_still_process_when_key_is_null() {
        var event = new BudgetApprovalRequestEvent();
        event.setServiceOrderId(2L);
        event.setToken("another-token");

        consumer.consume(event, null);

        verify(sendBudgetApprovalEmailUseCase).send(2L, "another-token");
    }

    @Test
    void should_still_process_when_key_mismatches() {
        var event = new BudgetApprovalRequestEvent();
        event.setServiceOrderId(3L);
        event.setToken("mismatched-key-token");

        consumer.consume(event, "999");

        verify(sendBudgetApprovalEmailUseCase).send(3L, "mismatched-key-token");
    }
}
