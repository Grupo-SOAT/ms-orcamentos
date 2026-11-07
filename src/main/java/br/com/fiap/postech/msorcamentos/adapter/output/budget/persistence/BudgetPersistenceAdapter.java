package br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence;

import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetItemEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.entity.BudgetItemSupplyEntity;
import br.com.fiap.postech.msorcamentos.adapter.output.budget.persistence.repository.BudgetRepository;
import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItem;
import br.com.fiap.postech.msorcamentos.domain.budget.model.BudgetItemSupply;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BudgetPersistenceAdapter implements BudgetPersistencePort {

    private final BudgetRepository repository;

    @Override
    public Budget save(Budget budget) {
        BudgetEntity saved = repository.save(toEntity(budget));
        return toDomain(saved);
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return repository.findById(id).map(BudgetPersistenceAdapter::toDomain);
    }

    static BudgetEntity toEntity(Budget budget) {
        BudgetEntity entity = BudgetEntity.builder()
                .id(budget.getId())
                .serviceOrderId(budget.getServiceOrderId())
                .totalAmount(budget.getTotalAmount())
                .currency(budget.getCurrency())
                .status(budget.getStatus())
                .createdAt(budget.getCreatedAt())
                .sentAt(budget.getSentAt())
                .build();

        entity.setItems(budget.getItems().stream()
                .map(item -> toItemEntity(entity, item))
                .toList());
        return entity;
    }

    private static BudgetItemEntity toItemEntity(BudgetEntity budget, BudgetItem item) {
        BudgetItemEntity itemEntity = BudgetItemEntity.builder()
                .budget(budget)
                .catalogServiceId(item.catalogServiceId())
                .unitPrice(item.unitPrice())
                .quantity(item.quantity())
                .lineTotal(item.lineTotal())
                .priceFetchedAt(item.priceFetchedAt())
                .build();
        itemEntity.setSupplies(item.supplies().stream()
                .map(supply -> toSupplyEntity(itemEntity, supply))
                .toList());
        return itemEntity;
    }

    private static BudgetItemSupplyEntity toSupplyEntity(BudgetItemEntity item, BudgetItemSupply supply) {
        return BudgetItemSupplyEntity.builder()
                .budgetItem(item)
                .supplyId(supply.supplyId())
                .unitPrice(supply.unitPrice())
                .quantity(supply.quantity())
                .lineTotal(supply.lineTotal())
                .priceFetchedAt(supply.priceFetchedAt())
                .build();
    }

    static Budget toDomain(BudgetEntity entity) {
        return Budget.restore(
                entity.getId(),
                entity.getServiceOrderId(),
                entity.getCurrency(),
                entity.getItems().stream().map(BudgetPersistenceAdapter::toItemDomain).toList(),
                entity.getTotalAmount(),
                entity.getCreatedAt(),
                entity.getSentAt(),
                entity.getStatus()
        );
    }

    private static BudgetItem toItemDomain(BudgetItemEntity entity) {
        return new BudgetItem(
                entity.getCatalogServiceId(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getSupplies().stream().map(BudgetPersistenceAdapter::toSupplyDomain).toList(),
                entity.getPriceFetchedAt()
        );
    }

    private static BudgetItemSupply toSupplyDomain(BudgetItemSupplyEntity entity) {
        return new BudgetItemSupply(
                entity.getSupplyId(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getPriceFetchedAt()
        );
    }
}
