package br.com.fiap.postech.msorcamentos.domain.payment.model;

public class InvalidPaymentTransitionException extends RuntimeException {
    public InvalidPaymentTransitionException(PaymentStatus status) {
        super("Invalid payment transition from status: " + status);
    }
}
