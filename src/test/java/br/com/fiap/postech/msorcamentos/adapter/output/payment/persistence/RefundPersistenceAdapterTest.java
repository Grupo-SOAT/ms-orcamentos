package br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.entity.RefundEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.payment.persistence.repository.RefundRepository;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Refund;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefundPersistenceAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final RefundRepository repository = mock(RefundRepository.class);
    private final RefundPersistenceAdapter adapter = new RefundPersistenceAdapter(repository);

    @Test
    void should_save_and_map_back() {
        RefundEntity entity = RefundEntity.builder()
                .id(1L).paymentId(10L).serviceOrderId(7L).amount(BigDecimal.TEN)
                .status("APPROVED").createdAt(NOW).build();
        when(repository.save(any())).thenReturn(entity);

        Refund saved = adapter.save(Refund.create(10L, 7L, BigDecimal.TEN, NOW));

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getPaymentId()).isEqualTo(10L);
    }

    @Test
    void should_find_by_payment_id() {
        RefundEntity entity = RefundEntity.builder()
                .id(1L).paymentId(10L).serviceOrderId(7L).amount(BigDecimal.TEN)
                .status("APPROVED").createdAt(NOW).build();
        when(repository.findByPaymentId(10L)).thenReturn(Optional.of(entity));

        assertThat(adapter.findByPaymentId(10L)).isPresent();
    }

    @Test
    void should_return_empty_when_refund_not_found() {
        when(repository.findByPaymentId(10L)).thenReturn(Optional.empty());

        assertThat(adapter.findByPaymentId(10L)).isEmpty();
    }
}
