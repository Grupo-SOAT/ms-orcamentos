package br.com.fiap.postech.msorcamentos.domain.payment.model;

public class PaymentDecisionAlreadyUsedException extends RuntimeException {
    public PaymentDecisionAlreadyUsedException(Long serviceOrderId) {
        super("Payment decision already used for service order: " + serviceOrderId);
    }
}
