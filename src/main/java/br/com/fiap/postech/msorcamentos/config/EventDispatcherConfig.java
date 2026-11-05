package br.com.fiap.postech.msorcamentos.config;

import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.BudgetRequestedHandler;
import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.BudgetRequestedEvent;
import br.com.fiap.postech.msorcamentos.adapter.input.budget.message.event.EventDispatcher;
import br.com.fiap.postech.msorcamentos.port.monitoring.EventMetricsPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class EventDispatcherConfig {

    @Bean
    public EventDispatcher eventDispatcher(
            ObjectMapper eventObjectMapper,
            EventMetricsPort metrics,
            BudgetRequestedHandler budgetRequestedHandler,
            @Value("${app.events.topic.service-order}") String topic
    ) {
        return new EventDispatcher(
                eventObjectMapper,
                topic,
                metrics,
                Map.of(BudgetRequestedEvent.class, event -> budgetRequestedHandler.handle((BudgetRequestedEvent) event))
        );
    }
}
