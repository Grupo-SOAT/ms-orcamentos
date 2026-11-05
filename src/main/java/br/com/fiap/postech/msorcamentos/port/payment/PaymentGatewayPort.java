package br.com.fiap.postech.msorcamentos.port.payment;

import java.math.BigDecimal;

public interface PaymentGatewayPort {

    Preference createPreference(Long serviceOrderId, BigDecimal amount, String description,
                                String idempotencyKey);

    GatewayPayment findPayment(String paymentId);

    RefundResult refund(String paymentId, BigDecimal amount, String idempotencyKey);

    record Preference(String preferenceId, String initPoint) {
    }

    record GatewayPayment(String paymentId, String status, Long externalReference) {
    }

    record RefundResult(String refundId, String status, BigDecimal amount) {
    }
}
