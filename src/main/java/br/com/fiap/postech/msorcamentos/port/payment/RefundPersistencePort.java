package br.com.fiap.postech.msorcamentos.port.payment;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;

import java.util.Optional;

public interface RefundPersistencePort {

    Refund save(Refund refund);

    Optional<Refund> findByPaymentId(Long paymentId);
}
