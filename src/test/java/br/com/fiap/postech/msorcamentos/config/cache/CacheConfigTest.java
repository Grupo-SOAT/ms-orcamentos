package br.com.fiap.postech.msorcamentos.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void should_create_catalog_cache_with_ttl_and_max_size() {
        var manager = new CacheConfig().cacheManager(Duration.ofMinutes(30), 10);

        CaffeineCache cache = (CaffeineCache) manager.getCache(CacheConfig.CATALOG_SERVICE_CACHE);
        assertThat(cache).isNotNull();
        assertThat(manager.getCacheNames()).containsExactly(CacheConfig.CATALOG_SERVICE_CACHE);

        Cache<Object, Object> nativeCache = cache.getNativeCache();
        nativeCache.put("1", "price");
        assertThat(nativeCache.getIfPresent("1")).isEqualTo("price");
        assertThat(nativeCache.policy().eviction().orElseThrow().getMaximum()).isEqualTo(10);
    }
}
