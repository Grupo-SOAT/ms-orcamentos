package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.util.List;

/**
 * Incoming request to price a service order budget. It carries references only.
 */
public record BudgetRequest(
        Long serviceOrderId,
        String currency,
        List<BudgetRequestItem> items
) {

    public BudgetRequest {
        if (items == null) {
            items = List.of();
        }
    }

    public record BudgetRequestItem(
            Long catalogServiceId,
            int quantity,
            List<BudgetRequestSupply> supplies
    ) {

        public BudgetRequestItem {
            supplies = supplies == null ? List.of() : List.copyOf(supplies);
        }
    }

    public record BudgetRequestSupply(Long supplyId, int quantity) {
    }
}
