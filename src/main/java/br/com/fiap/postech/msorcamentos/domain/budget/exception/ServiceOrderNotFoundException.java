package br.com.fiap.postech.msorcamentos.domain.budget.exception;

public class ServiceOrderNotFoundException extends RuntimeException {

    public ServiceOrderNotFoundException(Long serviceOrderId) {
        super("Ordem de servico nao encontrada: " + serviceOrderId);
    }
}
