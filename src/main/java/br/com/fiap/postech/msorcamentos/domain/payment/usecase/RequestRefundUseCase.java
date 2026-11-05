package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import br.com.fiap.postech.msorcamentos.port.payment.RefundPersistencePort;

import java.time.Clock;
import java.time.Instant;

/**
 * Requests a full refund. The payment id is unique in the refund aggregate, so a duplicate request
 * returns the existing refund without calling the gateway again.
 */
public class RequestRefundUseCase {

    private final PaymentPersistencePort paymentPersistencePort;
    private final RefundPersistencePort refundPersistencePort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentEventPublisherPort eventPublisherPort;
    private final Clock clock;

    public RequestRefundUseCase(
            PaymentPersistencePort paymentPersistencePort,
            RefundPersistencePort refundPersistencePort,
            PaymentGatewayPort paymentGatewayPort,
            PaymentEventPublisherPort eventPublisherPort,
            Clock clock
    ) {
        this.paymentPersistencePort = paymentPersistencePort;
        this.refundPersistencePort = refundPersistencePort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.eventPublisherPort = eventPublisherPort;
        this.clock = clock;
    }

    public Refund refund(Long serviceOrderId, String correlationId) {
        Payment payment = paymentPersistencePort.findByServiceOrderId(serviceOrderId)
                .orElseThrow(() -> new PaymentWebhookUseCase.PaymentNotFoundException(serviceOrderId));

        var existing = refundPersistencePort.findByPaymentId(payment.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new IllegalStateException("Only approved payments can be refunded");
        }

        var result = paymentGatewayPort.refund(
                payment.getPaymentId(), payment.getAmount(), String.valueOf(payment.getId()));

        Instant now = Instant.now(clock);
        Refund refund = refundPersistencePort.save(
                Refund.create(payment.getId(), serviceOrderId, payment.getAmount(), now));
        payment.markRefunded(now);
        paymentPersistencePort.save(payment);
        eventPublisherPort.publish(PaymentEvent.of(
                PaymentEvent.PAYMENT_REFUNDED, payment, now, correlationId));
        return refund;
    }
}
