package br.com.fiap.postech.msorcamentos.config;

import br.com.fiap.postech.msorcamentos.domain.budget.usecase.RegisterBudgetDecisionUseCase;
import br.com.fiap.postech.msorcamentos.domain.budget.usecase.SendBudgetApprovalEmailUseCase;
import br.com.fiap.postech.msorcamentos.port.email.BudgetApprovalEmailSenderPort;
import br.com.fiap.postech.msorcamentos.port.message.BudgetDecisionPublisherPort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetApprovalTokenReadPort;
import br.com.fiap.postech.msorcamentos.port.persistence.ServiceOrderReadPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseDependencyInjectionConfig {

    @Bean
    public SendBudgetApprovalEmailUseCase sendBudgetApprovalEmailUseCase(
            ServiceOrderReadPort serviceOrderReadPort,
            BudgetApprovalEmailSenderPort budgetApprovalEmailSenderPort
    ) {
        return new SendBudgetApprovalEmailUseCase(serviceOrderReadPort, budgetApprovalEmailSenderPort);
    }

    @Bean
    public RegisterBudgetDecisionUseCase registerBudgetDecisionUseCase(
            BudgetApprovalTokenReadPort budgetApprovalTokenReadPort,
            BudgetDecisionPublisherPort budgetDecisionPublisherPort
    ) {
        return new RegisterBudgetDecisionUseCase(budgetApprovalTokenReadPort, budgetDecisionPublisherPort);
    }
}
