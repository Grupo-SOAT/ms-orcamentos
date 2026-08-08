package br.com.fiap.postech.msorcamentos.domain.budget.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetExceptionsTest {

    @Test
    void invalid_or_expired_token_exception_should_contain_service_order_id() {
        var exception = new InvalidOrExpiredBudgetTokenException(42L);

        assertThat(exception.getMessage()).contains("42");
    }

    @Test
    void invalid_budget_decision_exception_should_contain_decision() {
        var exception = new InvalidBudgetDecisionException("UNKNOWN");

        assertThat(exception.getMessage()).contains("UNKNOWN");
    }

    @Test
    void service_order_not_found_exception_should_contain_service_order_id() {
        var exception = new ServiceOrderNotFoundException(7L);

        assertThat(exception.getMessage()).contains("7");
    }
}
