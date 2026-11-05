package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompensatePaymentTimeoutUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final PaymentPersistencePort paymentPort = mock(PaymentPersistencePort.class);
    private final PaymentEventPublisherPort publisher = mock(PaymentEventPublisherPort.class);
    private final CompensatePaymentTimeoutUseCase useCase =
            new CompensatePaymentTimeoutUseCase(paymentPort, publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void should_expire_and_publish_timeout_event() {
        Payment expired = Payment.createPreference(7L, 3L, "pref", "url", BigDecimal.TEN,
                NOW.minusSeconds(7200), java.time.Duration.ofHours(1));
        when(paymentPort.findPendingExpiredBefore(NOW)).thenReturn(List.of(expired));
        when(paymentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Payment> result = useCase.compensate();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(PaymentEvent.PAYMENT_TIMEOUT);
        assertThat(captor.getValue().serviceOrderId()).isEqualTo(7L);
    }

    @Test
    void should_do_nothing_when_no_payment_expired() {
        when(paymentPort.findPendingExpiredBefore(NOW)).thenReturn(List.of());

        assertThat(useCase.compensate()).isEmpty();
    }
}
