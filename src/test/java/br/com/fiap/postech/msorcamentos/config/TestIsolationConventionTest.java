package br.com.fiap.postech.msorcamentos.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the unit/integration split.
 *
 * <p>Surefire runs only {@code *Test} classes. Those classes must never depend on
 * Testcontainers, otherwise {@code mvn test} and the mutation run would start containers.</p>
 */
class TestIsolationConventionTest {

    private static final Path TEST_SOURCES = Path.of("src/test/java");

    @Test
    void unitTestsMustNotDependOnTestcontainers() throws IOException {
        try (Stream<Path> files = Files.walk(TEST_SOURCES)) {
            List<String> offenders = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Test.java"))
                    .filter(path -> !path.getFileName().toString().equals("TestIsolationConventionTest.java"))
                    .filter(TestIsolationConventionTest::importsTestcontainers)
                    .map(path -> TEST_SOURCES.relativize(path).toString())
                    .sorted()
                    .toList();

            assertThat(offenders)
                    .as("Unit tests (*Test) must not import org.testcontainers; move them to *IT")
                    .isEmpty();
        }
    }

    private static boolean importsTestcontainers(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).contains("org.testcontainers");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }
}
