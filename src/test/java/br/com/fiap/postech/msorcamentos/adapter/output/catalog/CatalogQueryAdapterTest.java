package br.com.fiap.postech.msorcamentos.adapter.output.catalog;

import br.com.fiap.postech.msorcamentos.domain.budget.model.CatalogPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogQueryAdapterTest {

    private MockRestServiceServer server;
    private CatalogQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new CatalogQueryAdapter(builder, "http://localhost:8080", "local-key");
    }

    @Test
    void should_map_catalog_service_prices() {
        server.expect(requestTo("http://localhost:8080/catalog/services/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"basePrice":100.00,"supplies":{"10":5.00}}
                        """, MediaType.APPLICATION_JSON));

        CatalogPrice price = adapter.findPrice(1L);

        assertThat(price.catalogServiceId()).isEqualTo(1L);
        assertThat(price.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(price.supplyPrices().get(10L)).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(price.fetchedAt()).isNotNull();
    }

    @Test
    void should_default_missing_supplies_to_empty_map() {
        server.expect(requestTo("http://localhost:8080/catalog/services/1"))
                .andRespond(withSuccess("""
                        {"id":1,"basePrice":100.00}
                        """, MediaType.APPLICATION_JSON));

        assertThat(adapter.findPrice(1L).supplyPrices()).isEmpty();
    }

    @Test
    void should_reject_empty_response() {
        server.expect(requestTo("http://localhost:8080/catalog/services/9"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThatThrownBy(() -> adapter.findPrice(9L))
                .isInstanceOf(IllegalStateException.class);
    }
}
