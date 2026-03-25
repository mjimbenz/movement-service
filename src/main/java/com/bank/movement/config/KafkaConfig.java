package com.bank.movement.config;


import com.bank.events.MovementRequestedEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {


    @Bean
    public ProducerFactory<String, MovementRequestedEvent> movementProducerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");

        // AVRO
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        config.put("schema.registry.url", "http://schema-registry:8081");

        // Retries obligatorios para Avro
        config.put(ProducerConfig.RETRIES_CONFIG, 10);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 2000);

        // Timeouts
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60000);
        config.put("metadata.max.age.ms", 10000);

        // Idempotencia (evita duplicados)
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, MovementRequestedEvent> movementKafkaTemplate() {
        return new KafkaTemplate<>(movementProducerFactory());
    }
}