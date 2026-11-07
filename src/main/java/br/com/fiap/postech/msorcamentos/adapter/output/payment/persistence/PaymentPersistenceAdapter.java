package br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.entity.PaymentEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.repository.PaymentRepository;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentPersistencePort {

    private final PaymentRepository repository;

    @Override
    public Payment save(Payment payment) {
        return toDomain(repository.save(toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return repository.findById(id).map(PaymentPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Payment> findByBudgetId(Long budgetId) {
        return repository.findByBudgetId(budgetId).map(PaymentPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Payment> findByServiceOrderId(Long serviceOrderId) {
        return repository.findByServiceOrderId(serviceOrderId).map(PaymentPersistenceAdapter::toDomain);
    }

    @Override
    public List<Payment> findPendingExpiredBefore(Instant now) {
        return repository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, now).stream()
                .map(PaymentPersistenceAdapter::toDomain)
                .toList();
    }

    static PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .serviceOrderId(payment.getServiceOrderId())
                .budgetId(payment.getBudgetId())
                .preferenceId(payment.getPreferenceId())
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .initPoint(payment.getInitPoint())
                .createdAt(payment.getCreatedAt())
                .expiresAt(payment.getExpiresAt())
                .usedAt(payment.getUsedAt())
                .decidedAt(payment.getDecidedAt())
                .build();
    }

    static Payment toDomain(PaymentEntity entity) {
        return Payment.restore(entity.getId(), entity.getServiceOrderId(), entity.getBudgetId(),
                entity.getPreferenceId(), entity.getPaymentId(), entity.getStatus(), entity.getAmount(),
                entity.getInitPoint(), entity.getCreatedAt(), entity.getExpiresAt(), entity.getUsedAt(),
                entity.getDecidedAt());
    }
}
