package br.com.fiap.postech.msorcamentos.domain.budget.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItem;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItemSupply;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetRequest;
import br.com.fiap.postech.msorcamentos.domain.budget.model.CatalogPrice;
import br.com.fiap.postech.msorcamentos.port.catalog.CatalogQueryPort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Prices a budget from references by querying the catalog and persists the price audit.
 */
public class CreateBudgetUseCase {

    private final CatalogQueryPort catalogQueryPort;
    private final BudgetPersistencePort budgetPersistencePort;
    private final Clock clock;

    public CreateBudgetUseCase(
            CatalogQueryPort catalogQueryPort,
            BudgetPersistencePort budgetPersistencePort,
            Clock clock
    ) {
        this.catalogQueryPort = catalogQueryPort;
        this.budgetPersistencePort = budgetPersistencePort;
        this.clock = clock;
    }

    public Budget create(BudgetRequest request) {
        if (request.serviceOrderId() == null) {
            throw new IllegalArgumentException("serviceOrderId is required");
        }
        if (request.items().isEmpty()) {
            throw new IllegalArgumentException("budget must have at least one item");
        }

        List<BudgetItem> items = request.items().stream()
                .map(this::toItem)
                .toList();

        Budget budget = Budget.create(request.serviceOrderId(),
                request.currency() == null ? "BRL" : request.currency(),
                items,
                Instant.now(clock));
        return budgetPersistencePort.save(budget);
    }

    private BudgetItem toItem(BudgetRequest.BudgetRequestItem item) {
        CatalogPrice price = catalogQueryPort.findPrice(item.catalogServiceId());
        List<BudgetItemSupply> supplies = item.supplies().stream()
                .map(supply -> toSupply(supply, price))
                .toList();
        return new BudgetItem(item.catalogServiceId(), price.basePrice(), item.quantity(),
                supplies, price.fetchedAt());
    }

    private BudgetItemSupply toSupply(BudgetRequest.BudgetRequestSupply supply, CatalogPrice price) {
        BigDecimal unitPrice = price.supplyPrices().get(supply.supplyId());
        if (unitPrice == null) {
            throw new MissingSupplyPriceException(supply.supplyId());
        }
        return new BudgetItemSupply(supply.supplyId(), unitPrice, supply.quantity(), price.fetchedAt());
    }
}
