package br.com.fiap.postech.msorcamentos.config;

import br.com.fiap.postech.msorcamentos.domain.budget.usecase.CreateBudgetUseCase;
import br.com.fiap.postech.msorcamentos.port.catalog.CatalogQueryPort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BudgetConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CreateBudgetUseCase createBudgetUseCase(
            CatalogQueryPort catalogQueryPort,
            BudgetPersistencePort budgetPersistencePort,
            Clock clock
    ) {
        return new CreateBudgetUseCase(catalogQueryPort, budgetPersistencePort, clock);
    }
}
