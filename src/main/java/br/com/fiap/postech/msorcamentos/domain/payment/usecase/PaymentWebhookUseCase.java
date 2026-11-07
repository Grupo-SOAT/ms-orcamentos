package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

/**
 * Maps Mercado Pago payment statuses to the local payment aggregate and publishes terminal events.
 */
public class PaymentWebhookUseCase {

    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentPersistencePort paymentPersistencePort;
    private final PaymentEventPublisherPort eventPublisherPort;
    private final Clock clock;

    public PaymentWebhookUseCase(
            PaymentGatewayPort paymentGatewayPort,
            PaymentPersistencePort paymentPersistencePort,
            PaymentEventPublisherPort eventPublisherPort,
            Clock clock
    ) {
        this.paymentGatewayPort = paymentGatewayPort;
        this.paymentPersistencePort = paymentPersistencePort;
        this.eventPublisherPort = eventPublisherPort;
        this.clock = clock;
    }

    public Payment handle(String externalPaymentId, String correlationId) {
        PaymentGatewayPort.GatewayPayment gatewayPayment = paymentGatewayPort.findPayment(externalPaymentId);
        Payment payment = paymentPersistencePort.findByServiceOrderId(gatewayPayment.externalReference())
                .orElseThrow(() -> new PaymentNotFoundException(gatewayPayment.externalReference()));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment;
        }

        Instant now = Instant.now(clock);
        String status = gatewayPayment.status() == null ? "" : gatewayPayment.status().toLowerCase(Locale.ROOT);
        return switch (status) {
            case "approved" -> {
                payment.approve(externalPaymentId, now);
                Payment saved = paymentPersistencePort.save(payment);
                eventPublisherPort.publish(PaymentEvent.of(
                        PaymentEvent.PAYMENT_APPROVED, saved, now, correlationId));
                yield saved;
            }
            case "rejected", "cancelled" -> {
                payment.reject(externalPaymentId, now);
                Payment saved = paymentPersistencePort.save(payment);
                eventPublisherPort.publish(PaymentEvent.of(
                        PaymentEvent.PAYMENT_REJECTED, saved, now, correlationId));
                yield saved;
            }
            default -> payment;
        };
    }

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(Long serviceOrderId) {
            super("Payment not found for service order: " + serviceOrderId);
        }
    }
}
