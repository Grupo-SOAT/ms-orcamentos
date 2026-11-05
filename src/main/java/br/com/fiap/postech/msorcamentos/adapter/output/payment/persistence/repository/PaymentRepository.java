package br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.repository;

import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.entity.PaymentEntity;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByBudgetId(Long budgetId);

    Optional<PaymentEntity> findByServiceOrderId(Long serviceOrderId);

    List<PaymentEntity> findByStatusAndExpiresAtBefore(PaymentStatus status, Instant now);
}
