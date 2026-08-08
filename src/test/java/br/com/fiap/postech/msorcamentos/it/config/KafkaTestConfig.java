package br.com.fiap.postech.msorcamentos.it.config;

import br.com.fiap.postech.msorcamentos.it.util.KafkaTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Set;

@TestConfiguration
public class KafkaTestConfig {

    @Value("${app.budget.kafka.topic.request}")
    private String topicRequest;

    @Value("${app.budget.kafka.topic.decision}")
    private String topicDecision;

    @Bean
    public KafkaTestHelper kafkaTestHelper(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaTestHelper(kafkaTemplate, bootstrapServers, new ObjectMapper(),
                Set.of(topicRequest, topicDecision));
    }
}
