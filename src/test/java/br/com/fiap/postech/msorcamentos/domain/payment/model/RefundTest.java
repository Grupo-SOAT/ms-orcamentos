package br.com.fiap.postech.msorcamentos.domain.payment.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void should_create_approved_refund() {
        Refund refund = Refund.create(10L, 7L, BigDecimal.TEN, NOW);

        assertThat(refund.getPaymentId()).isEqualTo(10L);
        assertThat(refund.getServiceOrderId()).isEqualTo(7L);
        assertThat(refund.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(refund.getStatus()).isEqualTo("APPROVED");
        assertThat(refund.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void should_restore_refund() {
        Refund refund = Refund.restore(1L, 10L, 7L, BigDecimal.TEN, "APPROVED", NOW);

        assertThat(refund.getId()).isEqualTo(1L);
        assertThat(refund.getPaymentId()).isEqualTo(10L);
    }

    @Test
    void should_reject_missing_required_fields() {
        assertThatThrownBy(() -> Refund.create(null, 7L, BigDecimal.TEN, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Refund.create(10L, null, BigDecimal.TEN, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Refund.create(10L, 7L, null, NOW))
                .isInstanceOf(NullPointerException.class);
    }
}
