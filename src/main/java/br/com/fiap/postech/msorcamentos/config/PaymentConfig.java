package br.com.fiap.postech.msorcamentos.config;

import br.com.fiap.postech.msorcamentos.domain.payment.usecase.CompensatePaymentTimeoutUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.CreatePaymentPreferenceUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.PaymentWebhookUseCase;
import br.com.fiap.postech.msorcamentos.domain.payment.usecase.RequestRefundUseCase;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import br.com.fiap.postech.msorcamentos.port.payment.RefundPersistencePort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableScheduling
public class PaymentConfig {

    @Bean
    public CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase(
            BudgetPersistencePort budgetPersistencePort,
            PaymentPersistencePort paymentPersistencePort,
            PaymentGatewayPort paymentGatewayPort,
            Clock clock,
            @Value("${app.payment.preference-ttl:PT1H}") Duration preferenceTtl
    ) {
        return new CreatePaymentPreferenceUseCase(budgetPersistencePort, paymentPersistencePort,
                paymentGatewayPort, clock, preferenceTtl);
    }

    @Bean
    public PaymentWebhookUseCase paymentWebhookUseCase(
            PaymentGatewayPort paymentGatewayPort,
            PaymentPersistencePort paymentPersistencePort,
            PaymentEventPublisherPort eventPublisherPort,
            Clock clock
    ) {
        return new PaymentWebhookUseCase(paymentGatewayPort, paymentPersistencePort,
                eventPublisherPort, clock);
    }

    @Bean
    public RequestRefundUseCase requestRefundUseCase(
            PaymentPersistencePort paymentPersistencePort,
            RefundPersistencePort refundPersistencePort,
            PaymentGatewayPort paymentGatewayPort,
            PaymentEventPublisherPort eventPublisherPort,
            Clock clock
    ) {
        return new RequestRefundUseCase(paymentPersistencePort, refundPersistencePort,
                paymentGatewayPort, eventPublisherPort, clock);
    }

    @Bean
    public CompensatePaymentTimeoutUseCase compensatePaymentTimeoutUseCase(
            PaymentPersistencePort paymentPersistencePort,
            PaymentEventPublisherPort eventPublisherPort,
            Clock clock
    ) {
        return new CompensatePaymentTimeoutUseCase(paymentPersistencePort, eventPublisherPort, clock);
    }

    @Bean
    public PaymentTimeoutScheduler paymentTimeoutScheduler(
            CompensatePaymentTimeoutUseCase compensatePaymentTimeoutUseCase
    ) {
        return new PaymentTimeoutScheduler(compensatePaymentTimeoutUseCase);
    }

    public static class PaymentTimeoutScheduler {

        private final CompensatePaymentTimeoutUseCase useCase;

        public PaymentTimeoutScheduler(CompensatePaymentTimeoutUseCase useCase) {
            this.useCase = useCase;
        }

        @Scheduled(fixedDelayString = "${app.payment.timeout-check-ms:60000}")
        public void compensateExpiredPayments() {
            useCase.compensate();
        }
    }
}
