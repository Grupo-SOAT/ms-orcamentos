package br.com.fiap.postech.msorcamentos.domain.payment.usecase;

import br.com.fiap.postech.msorcamentos.domain.payment.model.Payment;
import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;
import br.com.fiap.postech.msorcamentos.port.message.PaymentEventPublisherPort;
import br.com.fiap.postech.msorcamentos.port.payment.PaymentPersistencePort;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Expires pending payments whose window elapsed and publishes the compensation event so the
 * service order can move to CANCELLED.
 */
public class CompensatePaymentTimeoutUseCase {

    private final PaymentPersistencePort paymentPersistencePort;
    private final PaymentEventPublisherPort eventPublisherPort;
    private final Clock clock;

    public CompensatePaymentTimeoutUseCase(
            PaymentPersistencePort paymentPersistencePort,
            PaymentEventPublisherPort eventPublisherPort,
            Clock clock
    ) {
        this.paymentPersistencePort = paymentPersistencePort;
        this.eventPublisherPort = eventPublisherPort;
        this.clock = clock;
    }

    public List<Payment> compensate() {
        Instant now = Instant.now(clock);
        return paymentPersistencePort.findPendingExpiredBefore(now).stream()
                .map(payment -> {
                    payment.expire(now);
                    Payment saved = paymentPersistencePort.save(payment);
                    eventPublisherPort.publish(PaymentEvent.of(
                            PaymentEvent.PAYMENT_TIMEOUT, saved, now, null));
                    return saved;
                })
                .toList();
    }
}
