package br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.repository;

import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.entity.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<RefundEntity, Long> {

    Optional<RefundEntity> findByPaymentId(Long paymentId);
}
