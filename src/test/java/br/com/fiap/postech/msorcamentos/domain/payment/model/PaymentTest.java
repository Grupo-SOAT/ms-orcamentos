package br.com.fiap.postech.msorcamentos.domain.payment.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Duration TTL = Duration.ofHours(1);

    private Payment payment() {
        return Payment.createPreference(7L, 3L, "pref-1", "http://checkout", BigDecimal.TEN, NOW, TTL);
    }

    @Test
    void should_create_pending_preference() {
        Payment payment = payment();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.isActive(NOW)).isTrue();
        assertThat(payment.getExpiresAt()).isEqualTo(NOW.plus(TTL));
    }

    @Test
    void should_reject_missing_required_fields() {
        assertThatThrownBy(() -> Payment.createPreference(null, 3L, "p", "url", BigDecimal.TEN, NOW, TTL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Payment.createPreference(7L, null, "p", "url", BigDecimal.TEN, NOW, TTL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Payment.createPreference(7L, 3L, null, "url", BigDecimal.TEN, NOW, TTL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Payment.createPreference(7L, 3L, "p", "url", null, NOW, TTL))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_approve_pending_payment() {
        Payment payment = payment();

        payment.approve("mp-1", NOW);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getPaymentId()).isEqualTo("mp-1");
        assertThat(payment.getDecidedAt()).isEqualTo(NOW);
    }

    @Test
    void should_reject_pending_payment() {
        Payment payment = payment();

        payment.reject("mp-2", NOW);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void should_expire_pending_payment() {
        Payment payment = payment();

        payment.expire(NOW);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    void should_reject_transition_when_not_pending() {
        Payment payment = payment();
        payment.approve("mp-1", NOW);

        assertThatThrownBy(() -> payment.reject("mp-2", NOW))
                .isInstanceOf(InvalidPaymentTransitionException.class);
        assertThatThrownBy(() -> payment.expire(NOW))
                .isInstanceOf(InvalidPaymentTransitionException.class);
    }

    @Test
    void should_mark_refunded_only_when_approved() {
        Payment payment = payment();
        payment.approve("mp-1", NOW);

        payment.markRefunded(NOW);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

        Payment pending = payment();
        assertThatThrownBy(() -> pending.markRefunded(NOW)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_consume_decision_only_once() {
        Payment payment = payment();
        payment.markDecisionUsed(NOW);

        assertThat(payment.getUsedAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> payment.markDecisionUsed(NOW))
                .isInstanceOf(PaymentDecisionAlreadyUsedException.class);
    }

    @Test
    void should_be_inactive_after_expiration() {
        Payment payment = payment();

        assertThat(payment.isActive(NOW.plus(TTL))).isFalse();
    }

    @Test
    void should_restore_all_fields() {
        Payment payment = Payment.restore(1L, 7L, 3L, "pref", "mp", PaymentStatus.APPROVED, BigDecimal.TEN,
                "url", NOW, NOW.plus(TTL), NOW, NOW);

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getBudgetId()).isEqualTo(3L);
        assertThat(payment.getPreferenceId()).isEqualTo("pref");
        assertThat(payment.getPaymentId()).isEqualTo("mp");
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(payment.getInitPoint()).isEqualTo("url");
        assertThat(payment.getCreatedAt()).isEqualTo(NOW);
        assertThat(payment.getUsedAt()).isEqualTo(NOW);
        assertThat(payment.getDecidedAt()).isEqualTo(NOW);
    }
}
