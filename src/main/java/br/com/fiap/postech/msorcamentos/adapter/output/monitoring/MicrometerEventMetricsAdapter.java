package br.com.fiap.postech.msorcamentos.adapter.output.monitoring;

import br.com.fiap.postech.msorcamentos.port.monitoring.EventMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MicrometerEventMetricsAdapter implements EventMetricsPort {

    private final MeterRegistry meterRegistry;

    public MicrometerEventMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordConsumed(String topic, String type) {
        counter("events_consumed_total", topic, type).increment();
    }

    @Override
    public void recordIgnored(String topic, String type) {
        counter("events_ignored_total", topic, type).increment();
    }

    private Counter counter(String name, String topic, String type) {
        return Counter.builder(name)
                .tag("service", "ms-orcamentos")
                .tag("topic", topic)
                .tag("type", type == null ? "unknown" : type)
                .register(meterRegistry);
    }
}
