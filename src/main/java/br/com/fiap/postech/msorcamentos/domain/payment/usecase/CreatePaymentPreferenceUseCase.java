package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.budget.model.Budget;
import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;
import br.com.fiap.postech.msorcamentos.port.persistence.BudgetPersistencePort;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Creates the Checkout Pro preference on approval click and reuses it while it is still active.
 */
public class CreatePaymentPreferenceUseCase {

    private final BudgetPersistencePort budgetPersistencePort;
    private final PaymentPersistencePort paymentPersistencePort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final Clock clock;
    private final Duration preferenceTtl;

    public CreatePaymentPreferenceUseCase(
            BudgetPersistencePort budgetPersistencePort,
            PaymentPersistencePort paymentPersistencePort,
            PaymentGatewayPort paymentGatewayPort,
            Clock clock,
            Duration preferenceTtl
    ) {
        this.budgetPersistencePort = budgetPersistencePort;
        this.paymentPersistencePort = paymentPersistencePort;
        this.paymentGatewayPort = paymentGatewayPort;
        this.clock = clock;
        this.preferenceTtl = preferenceTtl;
    }

    public PreferenceResult createOrReuse(Long budgetId) {
        Budget budget = budgetPersistencePort.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));

        Instant now = Instant.now(clock);
        var existing = paymentPersistencePort.findByBudgetId(budgetId);
        if (existing.isPresent() && existing.get().isActive(now)) {
            return new PreferenceResult(existing.get().getPreferenceId(), existing.get().getInitPoint(),
                    existing.get().getPaymentId() == null ? null : existing.get().getPaymentId());
        }

        var preference = paymentGatewayPort.createPreference(
                budget.getServiceOrderId(),
                budget.getTotalAmount(),
                "Orcamento da OS " + budget.getServiceOrderId(),
                "budget-" + budgetId);

        Payment payment = Payment.createPreference(budget.getServiceOrderId(), budgetId,
                preference.preferenceId(), preference.initPoint(), budget.getTotalAmount(), now, preferenceTtl);
        Payment saved = paymentPersistencePort.save(payment);
        return new PreferenceResult(saved.getPreferenceId(), saved.getInitPoint(), null);
    }

    public record PreferenceResult(String preferenceId, String initPoint, String paymentId) {
    }

    public static class BudgetNotFoundException extends RuntimeException {
        public BudgetNotFoundException(Long id) {
            super("Budget not found for id: " + id);
        }
    }
}
