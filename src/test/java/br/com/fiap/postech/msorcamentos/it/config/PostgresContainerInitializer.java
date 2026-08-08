package br.com.fiap.postech.msorcamentos.it.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16.4")
                    .withDatabaseName("workshop_test")
                    .withUsername("test")
                    .withPassword("test");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!POSTGRESQL_CONTAINER.isRunning()) {
            POSTGRESQL_CONTAINER.start();
        }

        TestPropertyValues.of(
                "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword(),
                "spring.datasource.driver-class-name=" + POSTGRESQL_CONTAINER.getDriverClassName()
        ).applyTo(applicationContext.getEnvironment());
    }
}
