package br.com.fiap.postech.msorcamentos.port.message;

import br.com.fiap.postech.msorcamentos.domain.payment.model.PaymentEvent;

public interface PaymentEventPublisherPort {

    void publish(PaymentEvent event);
}
