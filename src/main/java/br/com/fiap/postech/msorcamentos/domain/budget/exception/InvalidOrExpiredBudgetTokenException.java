package br.com.fiap.postech.msorcamentos.domain.budget.exception;

public class InvalidOrExpiredBudgetTokenException extends RuntimeException {

    public InvalidOrExpiredBudgetTokenException(Long serviceOrderId) {
        super("Token de aprovacao de orcamento invalido, expirado ou ja utilizado para a OS " + serviceOrderId);
    }
}
