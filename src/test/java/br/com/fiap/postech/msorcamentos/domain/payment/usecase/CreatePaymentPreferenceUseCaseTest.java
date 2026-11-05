package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItem;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreatePaymentPreferenceUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Duration TTL = Duration.ofHours(1);

    private final BudgetPersistencePort budgetPort = mock(BudgetPersistencePort.class);
    private final PaymentPersistencePort paymentPort = mock(PaymentPersistencePort.class);
    private final PaymentGatewayPort gateway = mock(PaymentGatewayPort.class);
    private final CreatePaymentPreferenceUseCase useCase = new CreatePaymentPreferenceUseCase(
            budgetPort, paymentPort, gateway, Clock.fixed(NOW, ZoneOffset.UTC), TTL);

    @Test
    void should_create_preference_for_budget() {
        Budget budget = Budget.create(7L, "BRL", List.of(new BudgetItem(1L, BigDecimal.TEN, 1, List.of(), NOW)), NOW);
        budget.assignId(3L);
        when(budgetPort.findById(3L)).thenReturn(Optional.of(budget));
        when(paymentPort.findByBudgetId(3L)).thenReturn(Optional.empty());
        when(gateway.createPreference(7L, BigDecimal.TEN, "Orcamento da OS 7", "budget-3"))
                .thenReturn(new PaymentGatewayPort.Preference("pref-1", "http://checkout"));
        when(paymentPort.save(any())).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return Payment.restore(1L, payment.getServiceOrderId(), payment.getBudgetId(),
                    payment.getPreferenceId(), payment.getPaymentId(), payment.getStatus(),
                    payment.getAmount(), payment.getInitPoint(), payment.getCreatedAt(),
                    payment.getExpiresAt(), payment.getUsedAt(), payment.getDecidedAt());
        });

        var result = useCase.createOrReuse(3L);

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(new CreatePaymentPreferenceUseCase.PreferenceResult("pref-1", "http://checkout", null));
        verify(paymentPort).save(any());
    }

    @Test
    void should_reuse_active_preference() {
        Budget budget = Budget.create(7L, "BRL", List.of(new BudgetItem(1L, BigDecimal.TEN, 1, List.of(), NOW)), NOW);
        budget.assignId(3L);
        Payment existing = Payment.createPreference(7L, 3L, "pref-old", "http://old", BigDecimal.TEN, NOW, TTL);
        when(budgetPort.findById(3L)).thenReturn(Optional.of(budget));
        when(paymentPort.findByBudgetId(3L)).thenReturn(Optional.of(existing));

        var result = useCase.createOrReuse(3L);

        assertThat(result.preferenceId()).isEqualTo("pref-old");
        verify(gateway, never()).createPreference(any(), any(), any(), any());
    }

    @Test
    void should_create_new_preference_when_existing_is_expired() {
        Budget budget = Budget.create(7L, "BRL", List.of(new BudgetItem(1L, BigDecimal.TEN, 1, List.of(), NOW)), NOW);
        budget.assignId(3L);
        Payment expired = Payment.createPreference(7L, 3L, "pref-old", "http://old", BigDecimal.TEN,
                NOW.minus(Duration.ofHours(2)), TTL);
        when(budgetPort.findById(3L)).thenReturn(Optional.of(budget));
        when(paymentPort.findByBudgetId(3L)).thenReturn(Optional.of(expired));
        when(gateway.createPreference(any(), any(), any(), any()))
                .thenReturn(new PaymentGatewayPort.Preference("pref-new", "http://new"));
        when(paymentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.createOrReuse(3L);

        assertThat(result.preferenceId()).isEqualTo("pref-new");
    }

    @Test
    void should_reject_missing_budget() {
        when(budgetPort.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.createOrReuse(9L))
                .isInstanceOf(CreatePaymentPreferenceUseCase.BudgetNotFoundException.class);
    }
}
