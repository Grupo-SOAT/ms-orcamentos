package br.com.fiap.postech.msorcamentos.domain.payment.model;

import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        String eventId,
        String type,
        Instant occurredAt,
        String correlationId,
        Long serviceOrderId,
        String paymentId
) {

    public static final String PAYMENT_APPROVED = "PAYMENT_APPROVED";
    public static final String PAYMENT_REJECTED = "PAYMENT_REJECTED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";
    public static final String PAYMENT_TIMEOUT = "PAYMENT_TIMEOUT";

    public static PaymentEvent of(String type, Payment payment, Instant now, String correlationId) {
        return new PaymentEvent(UUID.randomUUID().toString(), type, now, correlationId,
                payment.getServiceOrderId(), payment.getPaymentId());
    }
}
