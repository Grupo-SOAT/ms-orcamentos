package br.com.fiap.postech.msorcamentos.domain.budget.exception;

public class InvalidBudgetDecisionException extends RuntimeException {

    public InvalidBudgetDecisionException(String decision) {
        super("Decisao de orcamento invalida: " + decision);
    }
}
