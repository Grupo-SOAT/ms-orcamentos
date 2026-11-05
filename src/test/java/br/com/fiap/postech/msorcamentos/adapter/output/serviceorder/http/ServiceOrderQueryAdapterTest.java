package br.com.fiap.postech.msorcamentos.adapter.output.serviceorder.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ServiceOrderQueryAdapterTest {

    private MockRestServiceServer server;
    private ServiceOrderQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new ServiceOrderQueryAdapter(builder, "http://localhost:8082", "http://localhost:8080");
    }

    @Test
    void should_compose_summary_from_service_order_and_owner() {
        server.expect(requestTo("http://localhost:8082/service-orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"clientId":99,"description":"Troca de oleo","estimatedAmount":250.00}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8080/owners/99"))
                .andRespond(withSuccess("""
                        {"id":99,"name":"Joao","email":"joao@email.com"}
                        """, MediaType.APPLICATION_JSON));

        var summary = adapter.findSummaryById(1L);

        assertThat(summary).isPresent();
        assertThat(summary.get().serviceOrderId()).isEqualTo(1L);
        assertThat(summary.get().clientName()).isEqualTo("Joao");
        assertThat(summary.get().clientEmail()).isEqualTo("joao@email.com");
        assertThat(summary.get().description()).isEqualTo("Troca de oleo");
        server.verify();
    }

    @Test
    void should_return_empty_when_service_order_is_missing() {
        server.expect(requestTo("http://localhost:8082/service-orders/1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(adapter.findSummaryById(1L)).isEmpty();
    }

    @Test
    void should_return_empty_when_owner_is_missing() {
        server.expect(requestTo("http://localhost:8082/service-orders/1"))
                .andRespond(withSuccess("""
                        {"id":1,"clientId":99,"description":"Troca","estimatedAmount":10.00}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8080/owners/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(adapter.findSummaryById(1L)).isEmpty();
    }
}
