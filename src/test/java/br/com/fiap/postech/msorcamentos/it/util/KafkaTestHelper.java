package br.com.fiap.postech.msorcamentos.it.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KafkaTestHelper {

    private static final String TEST_GROUP_ID = "ms-orcamentos-test-group";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String bootstrapServers;
    private final ObjectMapper objectMapper;
    private final Set<String> allTopics;

    public KafkaTestHelper(
            KafkaTemplate<String, Object> kafkaTemplate,
            String bootstrapServers,
            ObjectMapper objectMapper,
            Set<String> allTopics
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.bootstrapServers = bootstrapServers;
        this.objectMapper = objectMapper;
        this.allTopics = allTopics;
    }

    public void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload);
        kafkaTemplate.flush();
    }

    public ConsumerRecord<String, String> receive(String topic, long timeoutSeconds) {
        try (var consumer = createConsumer(TEST_GROUP_ID + "-" + System.nanoTime())) {
            consumer.subscribe(List.of(topic));
            var records = consumer.poll(Duration.ofSeconds(timeoutSeconds));
            if (records.isEmpty()) {
                return null;
            }
            return records.iterator().next();
        }
    }

    public void drainAll() {
        try (var consumer = createConsumer(TEST_GROUP_ID)) {
            consumer.subscribe(allTopics);
            consumer.poll(Duration.ofSeconds(2));
            consumer.commitSync();
        }
    }

    private KafkaConsumer<String, String> createConsumer(String groupId) {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        ));
    }

    public KafkaTemplate<String, Object> kafkaTemplate() {
        return kafkaTemplate;
    }
}
