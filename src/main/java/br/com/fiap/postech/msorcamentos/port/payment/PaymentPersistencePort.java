package br.com.fiap.postech.msorcamentos.port.payment;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentPersistencePort {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByBudgetId(Long budgetId);

    Optional<Payment> findByServiceOrderId(Long serviceOrderId);

    List<Payment> findPendingExpiredBefore(Instant now);
}
