package br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.entity.RefundEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.repository.RefundRepository;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;
import br.com.fiap.postech.msorcamentos.port.payment.RefundPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefundPersistenceAdapter implements RefundPersistencePort {

    private final RefundRepository repository;

    @Override
    public Refund save(Refund refund) {
        return toDomain(repository.save(toEntity(refund)));
    }

    @Override
    public Optional<Refund> findByPaymentId(Long paymentId) {
        return repository.findByPaymentId(paymentId).map(RefundPersistenceAdapter::toDomain);
    }

    static RefundEntity toEntity(Refund refund) {
        return RefundEntity.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .serviceOrderId(refund.getServiceOrderId())
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .build();
    }

    static Refund toDomain(RefundEntity entity) {
        return Refund.restore(entity.getId(), entity.getPaymentId(), entity.getServiceOrderId(),
                entity.getAmount(), entity.getStatus(), entity.getCreatedAt());
    }
}
