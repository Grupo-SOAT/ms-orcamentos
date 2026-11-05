package br.com.fiap.postech.msorcamentos.domain.budget.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Price snapshot returned by the registration service for a catalog service.
 */
public record CatalogPrice(
        Long catalogServiceId,
        BigDecimal basePrice,
        Map<Long, BigDecimal> supplyPrices,
        Instant fetchedAt
) {

    public CatalogPrice {
        if (catalogServiceId == null) {
            throw new IllegalArgumentException("catalogServiceId is required");
        }
        if (basePrice == null || basePrice.signum() < 0) {
            throw new IllegalArgumentException("basePrice must not be negative");
        }
        supplyPrices = supplyPrices == null ? Map.of() : Map.copyOf(supplyPrices);
        if (fetchedAt == null) {
            throw new IllegalArgumentException("fetchedAt is required");
        }
    }
}
