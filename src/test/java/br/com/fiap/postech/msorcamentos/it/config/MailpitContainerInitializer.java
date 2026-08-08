package br.com.fiap.postech.msorcamentos.it.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class MailpitContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final int SMTP_PORT = 1025;
    private static final int HTTP_PORT = 8025;

    private static final GenericContainer<?> MAILPIT_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("axllent/mailpit:latest"))
                    .withExposedPorts(SMTP_PORT, HTTP_PORT)
                    .waitingFor(Wait.forHttp("/api/v1/info").forPort(HTTP_PORT));

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!MAILPIT_CONTAINER.isRunning()) {
            MAILPIT_CONTAINER.start();
        }

        TestPropertyValues.of(
                "spring.mail.host=" + MAILPIT_CONTAINER.getHost(),
                "spring.mail.port=" + MAILPIT_CONTAINER.getMappedPort(SMTP_PORT)
        ).applyTo(applicationContext.getEnvironment());
    }

    public static String apiBaseUrl() {
        return "http://" + MAILPIT_CONTAINER.getHost() + ":" + MAILPIT_CONTAINER.getMappedPort(HTTP_PORT);
    }
}
