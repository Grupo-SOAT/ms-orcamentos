package br.com.fiap.postech.msorcamentos.it.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Runs WireMock for the registration and service order HTTP dependencies.
 */
public class WireMockContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> WIREMOCK =
            new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.13.1"))
                    .withExposedPorts(8080);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!WIREMOCK.isRunning()) {
            WIREMOCK.start();
        }
        String baseUrl = baseUrl();
        TestPropertyValues.of(
                "app.service-order.base-url=" + baseUrl,
                "app.registration.base-url=" + baseUrl
        ).applyTo(applicationContext.getEnvironment());
    }

    public static String baseUrl() {
        return "http://" + WIREMOCK.getHost() + ":" + WIREMOCK.getMappedPort(8080);
    }
}
