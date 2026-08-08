package br.com.fiap.postech.msorcamentos.it.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.1"))
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                    .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                    .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.start();
        }

        TestPropertyValues.of(
                "spring.kafka.bootstrap-servers=" + KAFKA_CONTAINER.getBootstrapServers()
        ).applyTo(applicationContext.getEnvironment());
    }
}
