package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentWebhookUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final PaymentGatewayPort gateway = mock(PaymentGatewayPort.class);
    private final PaymentPersistencePort paymentPort = mock(PaymentPersistencePort.class);
    private final PaymentEventPublisherPort publisher = mock(PaymentEventPublisherPort.class);
    private final PaymentWebhookUseCase useCase =
            new PaymentWebhookUseCase(gateway, paymentPort, publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    private Payment pending() {
        return Payment.createPreference(7L, 3L, "pref", "url", BigDecimal.TEN, NOW, Duration.ofHours(1));
    }

    @Test
    void should_approve_and_publish_when_gateway_says_approved() {
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", "approved", 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(pending()));
        when(paymentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = useCase.handle("mp-1", "corr-1");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(PaymentEvent.PAYMENT_APPROVED);
        assertThat(captor.getValue().correlationId()).isEqualTo("corr-1");
    }

    @Test
    void should_reject_and_publish_when_gateway_says_rejected() {
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", "rejected", 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(pending()));
        when(paymentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = useCase.handle("mp-1", "corr-1");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(PaymentEvent.PAYMENT_REJECTED);
    }

    @Test
    void should_treat_cancelled_as_rejected() {
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", "cancelled", 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(pending()));
        when(paymentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(useCase.handle("mp-1", "corr").getStatus()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void should_ignore_pending_gateway_status() {
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", "pending", 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(pending()));

        Payment result = useCase.handle("mp-1", "corr");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(publisher, never()).publish(any());
        verify(paymentPort, never()).save(any());
    }

    @Test
    void should_ignore_null_gateway_status() {
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", null, 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(pending()));

        assertThat(useCase.handle("mp-1", "corr").getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void should_return_already_decided_payment_without_republishing() {
        Payment approved = Payment.restore(1L, 7L, 3L, "pref", "mp-0", PaymentStatus.APPROVED, BigDecimal.TEN,
                "url", NOW, NOW.plusSeconds(3600), NOW, NOW);
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", "approved", 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(approved));

        assertThat(useCase.handle("mp-1", "corr").getStatus()).isEqualTo(PaymentStatus.APPROVED);
        verify(publisher, never()).publish(any());
    }

    @Test
    void should_reject_missing_local_payment() {
        when(gateway.findPayment("mp-1"))
                .thenReturn(new PaymentGatewayPort.GatewayPayment("mp-1", "approved", 7L));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.handle("mp-1", "corr"))
                .isInstanceOf(PaymentWebhookUseCase.PaymentNotFoundException.class);
    }
}
