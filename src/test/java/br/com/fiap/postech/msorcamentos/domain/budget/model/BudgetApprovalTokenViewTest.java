package br.com.fiap.postech.msorcamentos.domain.budget.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetApprovalTokenViewTest {

    @Test
    void should_be_valid_when_not_used_and_not_expired() {
        var view = new BudgetApprovalTokenView(1L, "token", Instant.now().plusSeconds(60), null);

        assertThat(view.isValid()).isTrue();
    }

    @Test
    void should_be_invalid_when_expired() {
        var view = new BudgetApprovalTokenView(1L, "token", Instant.now().minusSeconds(1), null);

        assertThat(view.isValid()).isFalse();
    }

    @Test
    void should_be_invalid_when_already_used() {
        var view = new BudgetApprovalTokenView(1L, "token", Instant.now().plusSeconds(60), Instant.now());

        assertThat(view.isValid()).isFalse();
    }
}
