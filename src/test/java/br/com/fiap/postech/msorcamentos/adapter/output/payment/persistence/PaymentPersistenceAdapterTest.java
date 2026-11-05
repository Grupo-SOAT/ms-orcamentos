package br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.entity.PaymentEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.repository.PaymentRepository;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentPersistenceAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final PaymentRepository repository = mock(PaymentRepository.class);
    private final PaymentPersistenceAdapter adapter = new PaymentPersistenceAdapter(repository);

    private PaymentEntity entity() {
        return PaymentEntity.builder()
                .id(1L)
                .serviceOrderId(7L)
                .budgetId(3L)
                .preferenceId("pref")
                .paymentId("mp")
                .status(PaymentStatus.PENDING)
                .amount(BigDecimal.TEN)
                .initPoint("url")
                .createdAt(NOW)
                .expiresAt(NOW.plusSeconds(3600))
                .build();
    }

    @Test
    void should_save_and_map_back() {
        when(repository.save(any())).thenReturn(entity());
        Payment payment = Payment.restore(null, 7L, 3L, "pref", "mp", PaymentStatus.PENDING, BigDecimal.TEN,
                "url", NOW, NOW.plusSeconds(3600), null, null);

        Payment saved = adapter.save(payment);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void should_find_by_id_budget_and_service_order() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity()));
        when(repository.findByBudgetId(3L)).thenReturn(Optional.of(entity()));
        when(repository.findByServiceOrderId(7L)).thenReturn(Optional.of(entity()));

        assertThat(adapter.findById(1L)).isPresent();
        assertThat(adapter.findByBudgetId(3L)).isPresent();
        assertThat(adapter.findByServiceOrderId(7L)).isPresent();
    }

    @Test
    void should_return_empty_when_not_found() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        when(repository.findByBudgetId(9L)).thenReturn(Optional.empty());
        when(repository.findByServiceOrderId(9L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(9L)).isEmpty();
        assertThat(adapter.findByBudgetId(9L)).isEmpty();
        assertThat(adapter.findByServiceOrderId(9L)).isEmpty();
    }

    @Test
    void should_find_pending_expired_before() {
        when(repository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, NOW)).thenReturn(List.of(entity()));

        assertThat(adapter.findPendingExpiredBefore(NOW)).hasSize(1);
    }
}
