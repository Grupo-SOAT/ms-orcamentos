package br.com.fiap.postech.msorcamentos.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache configuration for catalog prices.
 *
 * <p>The TTL is an operational parameter supplied by {@code app.cache.catalog-service.ttl}
 * (ISO-8601 duration, default one hour).</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CATALOG_SERVICE_CACHE = "catalog-service";

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.catalog-service.ttl:PT1H}") Duration ttl,
            @Value("${app.cache.catalog-service.max-size:1000}") long maxSize
    ) {
        CaffeineCacheManager manager = new CaffeineCacheManager(CATALOG_SERVICE_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize));
        return manager;
    }
}
