package br.com.fiap.postech.msorcamentos.port.monitoring;

public interface EventMetricsPort {

    void recordConsumed(String topic, String type);

    void recordIgnored(String topic, String type);
}
