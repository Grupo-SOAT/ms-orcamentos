package br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event;

import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BudgetRequestedEvent extends EventEnvelope {

    private BudgetRequest payload;
}
