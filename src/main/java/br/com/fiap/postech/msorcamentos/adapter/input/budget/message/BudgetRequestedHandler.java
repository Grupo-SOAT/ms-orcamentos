package br.com.fiap.postech.msorcamentos.adapter.input.budget.message;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.BudgetRequestedEvent;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.message.event.BudgetCreatedEvent;
import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.CreateBudgetUseCase;
import br.com.fiap.postech.msorcamentos.port.message.BudgetEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetRequestedHandler {

    private final CreateBudgetUseCase createBudgetUseCase;
    private final BudgetEventPublisherPort eventPublisherPort;

    public void handle(BudgetRequestedEvent event) {
        Budget budget = createBudgetUseCase.create(event.getPayload());
        BudgetCreatedEvent created = BudgetCreatedEvent.of(budget, event.getCorrelationId(), Instant.now());
        eventPublisherPort.publish(created);
        log.info("Budget created for serviceOrderId={} budgetId={} total={}",
                budget.getServiceOrderId(), budget.getId(), budget.getTotalAmount());
    }
}
