package br.com.fiap.postech.msorcamentos.port.persistence;

import br.com.fiap.postech.msorcamentos.domain.budget.model.ServiceOrderSummary;

import java.util.Optional;

public interface ServiceOrderReadPort {

    Optional<ServiceOrderSummary> findSummaryById(Long serviceOrderId);
}
