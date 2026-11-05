package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import br.com.fiap.postech.msorcamentos.port.payment.RefundPersistencePort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
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

class RequestRefundUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final PaymentPersistencePort paymentPort = mock(PaymentPersistencePort.class);
    private final RefundPersistencePort refundPort = mock(RefundPersistencePort.class);
    private final PaymentGatewayPort gateway = mock(PaymentGatewayPort.class);
    private final PaymentEventPublisherPort publisher = mock(PaymentEventPublisherPort.class);
    private final RequestRefundUseCase useCase = new RequestRefundUseCase(
            paymentPort, refundPort, gateway, publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    private Payment approvedPayment() {
        return Payment.restore(10L, 7L, 3L, "pref", "mp-1", PaymentStatus.APPROVED, BigDecimal.TEN,
                "url", NOW, NOW.plusSeconds(3600), NOW, NOW);
    }

    @Test
    void should_refund_approved_payment_and_publish() {
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(approvedPayment()));
        when(refundPort.findByPaymentId(10L)).thenReturn(Optional.empty());
        when(gateway.refund("mp-1", BigDecimal.TEN, "10"))
                .thenReturn(new PaymentGatewayPort.RefundResult("ref-1", "approved", BigDecimal.TEN));
        when(refundPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Refund refund = useCase.refund(7L, "corr-1");

        assertThat(refund.getPaymentId()).isEqualTo(10L);
        assertThat(refund.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(PaymentEvent.PAYMENT_REFUNDED);
    }

    @Test
    void should_return_existing_refund_without_calling_gateway() {
        Refund existing = Refund.restore(1L, 10L, 7L, BigDecimal.TEN, "APPROVED", NOW);
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(approvedPayment()));
        when(refundPort.findByPaymentId(10L)).thenReturn(Optional.of(existing));

        assertThat(useCase.refund(7L, "corr")).isSameAs(existing);
        verify(gateway, never()).refund(any(), any(), any());
    }

    @Test
    void should_reject_refund_when_payment_is_not_approved() {
        Payment pending = Payment.createPreference(7L, 3L, "pref", "url", BigDecimal.TEN, NOW,
                java.time.Duration.ofHours(1));
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.of(pending));
        when(refundPort.findByPaymentId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refund(7L, "corr"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_reject_missing_payment() {
        when(paymentPort.findByServiceOrderId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refund(7L, "corr"))
                .isInstanceOf(PaymentWebhookUseCase.PaymentNotFoundException.class);
    }
}
