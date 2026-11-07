package br.com.fiap.postech.msorcamentos.adapter.output.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerEventMetricsAdapterTest {

    @Test
    void should_count_consumed_events() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerEventMetricsAdapter adapter = new MicrometerEventMetricsAdapter(registry);

        adapter.recordConsumed("budget-events", "BUDGET_REQUESTED");

        assertThat(registry.get("events_consumed_total")
                .tags("service", "ms-orcamentos", "topic", "budget-events", "type", "BUDGET_REQUESTED")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    void should_count_ignored_events_with_null_type() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerEventMetricsAdapter adapter = new MicrometerEventMetricsAdapter(registry);

        adapter.recordIgnored("budget-events", null);

        assertThat(registry.get("events_ignored_total")
                .tags("service", "ms-orcamentos", "topic", "budget-events", "type", "unknown")
                .counter().count()).isEqualTo(1.0);
    }
}
