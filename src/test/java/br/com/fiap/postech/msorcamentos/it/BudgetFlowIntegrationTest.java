package br.com.fiap.postech.msorcamentos.it;

import br.com.fiap.postech.msorcamentos.it.config.KafkaContainerInitializer;
import br.com.fiap.postech.msorcamentos.it.config.KafkaTestConfig;
import br.com.fiap.postech.msorcamentos.it.config.MailpitContainerInitializer;
import br.com.fiap.postech.msorcamentos.it.config.PostgresContainerInitializer;
import br.com.fiap.postech.msorcamentos.it.util.KafkaTestHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@Import(KafkaTestConfig.class)
@ContextConfiguration(initializers = {
        KafkaContainerInitializer.class,
        PostgresContainerInitializer.class,
        MailpitContainerInitializer.class
})
class BudgetFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KafkaTestHelper kafkaTestHelper;

    @Value("${app.budget.kafka.topic.request}")
    private String topicRequest;

    @Value("${app.budget.kafka.topic.decision}")
    private String topicDecision;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM budget_approval_tokens");
        jdbcTemplate.update("DELETE FROM service_orders");
        jdbcTemplate.update("DELETE FROM owners");
        restTemplate.delete(MailpitContainerInitializer.apiBaseUrl() + "/api/v1/messages");
    }

    @Test
    void should_send_email_when_budget_approval_request_is_consumed() throws Exception {
        long ownerId = 1L;
        long serviceOrderId = 100L;
        String token = UUID.randomUUID().toString();
        seed(ownerId, "cliente1@teste.com", serviceOrderId, token, Instant.now().plusSeconds(3600), null);

        kafkaTestHelper.send(topicRequest, String.valueOf(serviceOrderId),
                new BudgetApprovalRequestPayload(serviceOrderId, token));

        await().atMost(Duration.ofSeconds(20)).until(() -> countMessagesTo("cliente1@teste.com") >= 1);

        JsonNode message = findMessageTo("cliente1@teste.com");
        String messageId = message.get("ID").asText();
        JsonNode detail = objectMapper.readTree(
                restTemplate.getForObject(MailpitContainerInitializer.apiBaseUrl() + "/api/v1/message/" + messageId, String.class));

        assertThat(detail.get("HTML").asText()).contains("decision-page").contains(token);
    }

    @Test
    void should_publish_decision_when_token_is_valid() {
        long ownerId = 2L;
        long serviceOrderId = 200L;
        String token = UUID.randomUUID().toString();
        seed(ownerId, "cliente2@teste.com", serviceOrderId, token, Instant.now().plusSeconds(3600), null);

        var response = restTemplate.postForEntity(decisionUrl(serviceOrderId, "APPROVE", token), jsonRequest(), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        var record = kafkaTestHelper.receive(topicDecision, 15);
        assertThat(record).isNotNull();
        assertThat(record.value()).contains("\"serviceOrderId\":" + serviceOrderId).contains("\"decision\":\"APPROVE\"");
    }

    @Test
    void should_reject_when_token_is_missing() {
        long serviceOrderId = 300L;

        assertThatClientError(() -> restTemplate.postForEntity(
                "http://localhost:" + port + "/service-orders/" + serviceOrderId + "/budget/APPROVE",
                jsonRequest(), Void.class), HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_reject_when_token_does_not_exist() {
        long serviceOrderId = 400L;

        assertThatClientError(() -> restTemplate.postForEntity(
                decisionUrl(serviceOrderId, "APPROVE", UUID.randomUUID().toString()), jsonRequest(), Void.class),
                HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_reject_when_token_is_expired() {
        long ownerId = 5L;
        long serviceOrderId = 500L;
        String token = UUID.randomUUID().toString();
        seed(ownerId, "cliente5@teste.com", serviceOrderId, token, Instant.now().minusSeconds(10), null);

        assertThatClientError(() -> restTemplate.postForEntity(
                decisionUrl(serviceOrderId, "APPROVE", token), jsonRequest(), Void.class), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_reject_when_token_already_used() {
        long ownerId = 6L;
        long serviceOrderId = 600L;
        String token = UUID.randomUUID().toString();
        seed(ownerId, "cliente6@teste.com", serviceOrderId, token, Instant.now().plusSeconds(3600), Instant.now());

        assertThatClientError(() -> restTemplate.postForEntity(
                decisionUrl(serviceOrderId, "APPROVE", token), jsonRequest(), Void.class), HttpStatus.UNAUTHORIZED);
    }

    private void seed(long ownerId, String email, long serviceOrderId, String token, Instant expiresAt, Instant usedAt) {
        jdbcTemplate.update("INSERT INTO owners (owner_id, name, email) VALUES (?,?,?)",
                ownerId, "Cliente Teste " + ownerId, email);
        jdbcTemplate.update("INSERT INTO service_orders (service_order_id, client_id, description, estimated_amount) VALUES (?,?,?,?)",
                serviceOrderId, ownerId, "Troca de oleo", BigDecimal.valueOf(250));
        jdbcTemplate.update("INSERT INTO budget_approval_tokens (id, service_order_id, token, expires_at, created_at, used_at) VALUES (?,?,?,?,?,?)",
                serviceOrderId, serviceOrderId, token, Timestamp.from(expiresAt), Timestamp.from(Instant.now()),
                usedAt == null ? null : Timestamp.from(usedAt));
    }

    private String decisionUrl(long serviceOrderId, String decision, String token) {
        return "http://localhost:" + port + "/service-orders/" + serviceOrderId + "/budget/" + decision + "?token=" + token;
    }

    private HttpEntity<Void> jsonRequest() {
        var headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }

    private void assertThatClientError(Callable<?> call, HttpStatus expectedStatus) {
        try {
            call.call();
            throw new AssertionError("Expected " + expectedStatus + " but request succeeded");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(expectedStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long countMessagesTo(String email) {
        JsonNode message = findMessageTo(email);
        return message != null ? 1 : 0;
    }

    private JsonNode findMessageTo(String email) {
        try {
            String body = restTemplate.getForObject(MailpitContainerInitializer.apiBaseUrl() + "/api/v1/messages", String.class);
            JsonNode root = objectMapper.readTree(body);
            for (JsonNode msg : root.get("messages")) {
                for (JsonNode to : msg.get("To")) {
                    if (email.equalsIgnoreCase(to.get("Address").asText())) {
                        return msg;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private record BudgetApprovalRequestPayload(long serviceOrderId, String token) {
    }
}
