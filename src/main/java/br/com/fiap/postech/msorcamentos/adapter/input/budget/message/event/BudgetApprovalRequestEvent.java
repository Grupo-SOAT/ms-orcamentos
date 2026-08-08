package br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BudgetApprovalRequestEvent {

    private Long serviceOrderId;
    private String token;
}
