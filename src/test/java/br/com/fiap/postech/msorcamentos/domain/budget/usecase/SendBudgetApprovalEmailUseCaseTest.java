package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;
import br.com.fiap.postech.msorcamentos.port.email.BudgetApprovalEmailSenderPort;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SendBudgetApprovalEmailUseCaseTest {

    @Mock
    private ServiceOrderReadPort serviceOrderReadPort;

    @Mock
    private BudgetApprovalEmailSenderPort budgetApprovalEmailSenderPort;

    @InjectMocks
    private SendBudgetApprovalEmailUseCase useCase;

    @Test
    void should_send_email_when_service_order_exists() {
        var summary = new ServiceOrderSummary(1L, "Troca de oleo", BigDecimal.valueOf(250), "Joao", "joao@email.com");
        when(serviceOrderReadPort.findSummaryById(1L)).thenReturn(Optional.of(summary));

        useCase.send(1L, "some-token");

        verify(budgetApprovalEmailSenderPort).send(summary, "some-token");
    }

    @Test
    void should_throw_when_service_order_not_found() {
        when(serviceOrderReadPort.findSummaryById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.send(1L, "some-token"))
                .isInstanceOf(ServiceOrderNotFoundException.class);
    }
}
