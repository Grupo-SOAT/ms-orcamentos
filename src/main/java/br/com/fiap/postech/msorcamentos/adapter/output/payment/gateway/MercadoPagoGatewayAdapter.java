package br.com.fiap.postech.msorcamentos.adapter.output.payment.gateway;

import br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Mercado Pago Checkout Pro adapter. Only this class knows the external contract; the domain sees
 * {@link PaymentGatewayPort}.
 */
@Component
public class MercadoPagoGatewayAdapter implements PaymentGatewayPort {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    private final RestClient restClient;

    public MercadoPagoGatewayAdapter(
            RestClient.Builder builder,
            @Value("${app.mercadopago.base-url:https://api.mercadopago.com}") String baseUrl,
            @Value("${app.mercadopago.access-token:local-token}") String accessToken
    ) {
        this.restClient = builder.baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
    }

    @Override
    public Preference createPreference(Long serviceOrderId, BigDecimal amount, String description,
                                       String idempotencyKey) {
        PreferenceResponse response = restClient.post()
                .uri("/checkout/preferences")
                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                .body(Map.of(
                        "external_reference", String.valueOf(serviceOrderId),
                        "items", List.of(Map.of(
                                "title", description,
                                "quantity", 1,
                                "unit_price", amount))))
                .retrieve()
                .body(PreferenceResponse.class);
        if (response == null || response.id() == null) {
            throw new IllegalStateException("Mercado Pago returned no preference id");
        }
        return new Preference(response.id(), response.initPoint());
    }

    @Override
    public GatewayPayment findPayment(String paymentId) {
        PaymentResponse response = restClient.get()
                .uri("/v1/payments/{id}", paymentId)
                .retrieve()
                .body(PaymentResponse.class);
        if (response == null) {
            throw new IllegalStateException("Mercado Pago returned no payment for id: " + paymentId);
        }
        return new GatewayPayment(response.id() == null ? paymentId : response.id(),
                response.status(), response.externalReference());
    }

    @Override
    public RefundResult refund(String paymentId, BigDecimal amount, String idempotencyKey) {
        RefundResponse response = restClient.post()
                .uri("/v1/payments/{id}/refunds", paymentId)
                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                .body(Map.of("amount", amount))
                .retrieve()
                .body(RefundResponse.class);
        if (response == null) {
            throw new IllegalStateException("Mercado Pago returned no refund for payment: " + paymentId);
        }
        return new RefundResult(String.valueOf(response.id()), response.status(), response.amount());
    }

    public record PreferenceResponse(String id, @com.fasterxml.jackson.annotation.JsonProperty("init_point") String initPoint) {
    }

    public record PaymentResponse(String id, String status,
                                  @com.fasterxml.jackson.annotation.JsonProperty("external_reference") Long externalReference) {
    }

    public record RefundResponse(Long id, String status, BigDecimal amount) {
    }
}
