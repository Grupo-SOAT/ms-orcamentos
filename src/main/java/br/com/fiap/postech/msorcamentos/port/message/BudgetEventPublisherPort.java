package br.com.fiap.postech.msorcamentos.port.message;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetCreatedEvent;

public interface BudgetEventPublisherPort {

    void publish(BudgetCreatedEvent event);
}
