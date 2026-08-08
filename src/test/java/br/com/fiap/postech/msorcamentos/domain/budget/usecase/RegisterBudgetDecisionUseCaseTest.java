package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.exception.InvalidOrExpiredBudgetTokenException;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetApprovalTokenView;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetDecision;
import br.com.fiap.postech.msorcamentos.port.message.BudgetDecisionPublisherPort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetApprovalTokenReadPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterBudgetDecisionUseCaseTest {

    @Mock
    private BudgetApprovalTokenReadPort budgetApprovalTokenReadPort;

    @Mock
    private BudgetDecisionPublisherPort budgetDecisionPublisherPort;

    @InjectMocks
    private RegisterBudgetDecisionUseCase useCase;

    @Test
    void should_publish_decision_when_token_is_valid() {
        var token = "550e8400-e29b-41d4-a716-446655440000";
        var view = new BudgetApprovalTokenView(1L, token, Instant.now().plusSeconds(3600), null);
        when(budgetApprovalTokenReadPort.findToken(1L, token)).thenReturn(Optional.of(view));

        useCase.process(1L, token, BudgetDecision.APPROVE);

        verify(budgetDecisionPublisherPort).publish(1L, BudgetDecision.APPROVE);
    }

    @Test
    void should_reject_when_token_not_found() {
        when(budgetApprovalTokenReadPort.findToken(1L, "missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.process(1L, "missing", BudgetDecision.APPROVE))
                .isInstanceOf(InvalidOrExpiredBudgetTokenException.class);

        verify(budgetDecisionPublisherPort, never()).publish(eq(1L), any());
    }

    @Test
    void should_reject_when_token_expired() {
        var token = "expired-token";
        var view = new BudgetApprovalTokenView(1L, token, Instant.now().minusSeconds(10), null);
        when(budgetApprovalTokenReadPort.findToken(1L, token)).thenReturn(Optional.of(view));

        assertThatThrownBy(() -> useCase.process(1L, token, BudgetDecision.REJECT))
                .isInstanceOf(InvalidOrExpiredBudgetTokenException.class);

        verify(budgetDecisionPublisherPort, never()).publish(eq(1L), any());
    }

    @Test
    void should_reject_when_token_already_used() {
        var token = "used-token";
        var view = new BudgetApprovalTokenView(1L, token, Instant.now().plusSeconds(3600), Instant.now());
        when(budgetApprovalTokenReadPort.findToken(1L, token)).thenReturn(Optional.of(view));

        assertThatThrownBy(() -> useCase.process(1L, token, BudgetDecision.CANCEL))
                .isInstanceOf(InvalidOrExpiredBudgetTokenException.class);

        verify(budgetDecisionPublisherPort, never()).publish(eq(1L), any());
    }

    @Test
    void should_validate_token_without_publishing() {
        var token = "valid-token";
        var view = new BudgetApprovalTokenView(1L, token, Instant.now().plusSeconds(3600), null);
        when(budgetApprovalTokenReadPort.findToken(1L, token)).thenReturn(Optional.of(view));

        assertThatCode(() -> useCase.validateToken(1L, token)).doesNotThrowAnyException();

        verify(budgetDecisionPublisherPort, never()).publish(eq(1L), any());
    }
}
