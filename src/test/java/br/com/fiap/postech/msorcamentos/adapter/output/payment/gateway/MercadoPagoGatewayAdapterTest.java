package br.com.fiap.postech.msorcamentos.adapter.output.payment.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoGatewayAdapterTest {

    private MockRestServiceServer server;
    private MercadoPagoGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new MercadoPagoGatewayAdapter(builder, "http://localhost:8089", "token");
    }

    @Test
    void should_create_preference_with_idempotency_key() {
        server.expect(requestTo("http://localhost:8089/checkout/preferences"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Idempotency-Key", "budget-3"))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {"id":"pref-1","init_point":"http://checkout"}
                        """, MediaType.APPLICATION_JSON));

        var result = adapter.createPreference(7L, BigDecimal.TEN, "Orcamento", "budget-3");

        assertThat(result.preferenceId()).isEqualTo("pref-1");
        assertThat(result.initPoint()).isEqualTo("http://checkout");
    }

    @Test
    void should_reject_empty_preference_response() {
        server.expect(requestTo("http://localhost:8089/checkout/preferences"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.createPreference(7L, BigDecimal.TEN, "Orcamento", "k"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_find_payment() {
        server.expect(requestTo("http://localhost:8089/v1/payments/mp-1"))
                .andRespond(withSuccess("""
                        {"id":"mp-1","status":"approved","external_reference":7}
                        """, MediaType.APPLICATION_JSON));

        var payment = adapter.findPayment("mp-1");

        assertThat(payment).usingRecursiveComparison()
                .isEqualTo(new br.com.fiap.postech.msorcamentos.port.payment.PaymentGatewayPort.GatewayPayment(
                        "mp-1", "approved", 7L));
    }

    @Test
    void should_refund_with_idempotency_key() {
        server.expect(requestTo("http://localhost:8089/v1/payments/mp-1/refunds"))
                .andExpect(header("X-Idempotency-Key", "10"))
                .andRespond(withSuccess("""
                        {"id":1,"status":"approved","amount":10.00}
                        """, MediaType.APPLICATION_JSON));

        var result = adapter.refund("mp-1", BigDecimal.TEN, "10");

        assertThat(result.refundId()).isEqualTo("1");
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.TEN);
    }
}
