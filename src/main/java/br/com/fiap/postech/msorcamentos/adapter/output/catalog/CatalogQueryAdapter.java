package br.com.fiap.postech.msorcamentos.adapter.output.catalog;

import br.com.fiap.postech.msorcamentos.domain.budget.model.CatalogPrice;
import br.com.fiap.postech.msorcamentos.port.catalog.CatalogQueryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

/**
 * Queries the registration service for catalog prices. Successful responses are cached by
 * {@code catalogServiceId}; exceptions are not cached.
 */
@Component
public class CatalogQueryAdapter implements CatalogQueryPort {

    private final RestClient restClient;

    public CatalogQueryAdapter(RestClient.Builder builder,
                               @Value("${app.mnl.base-url:http://localhost:8080}") String baseUrl,
                               @Value("${app.internal.api-key:local-internal-key}") String internalApiKey) {
        this.restClient = builder.baseUrl(baseUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }

    @Override
    @Cacheable(cacheNames = "catalog-service", key = "#catalogServiceId")
    public CatalogPrice findPrice(Long catalogServiceId) {
        CatalogServiceResponse response = restClient.get()
                .uri("/catalog/services/{id}", catalogServiceId)
                .retrieve()
                .body(CatalogServiceResponse.class);
        if (response == null) {
            throw new IllegalStateException("Empty catalog service response for id: " + catalogServiceId);
        }
        return new CatalogPrice(response.id(), response.basePrice(),
                response.supplies() == null ? Map.of() : response.supplies(),
                Instant.now());
    }

    public record CatalogServiceResponse(Long id, java.math.BigDecimal basePrice,
                                         Map<Long, java.math.BigDecimal> supplies) {
    }
}
