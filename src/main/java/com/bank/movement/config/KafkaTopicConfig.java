package com.bank.movement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic movementRequestedTopic() {
        return TopicBuilder.name("movement-requested")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic movementCompletedTopic() {
        return TopicBuilder.name("movement-completed")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic movementRejectedTopic() {
        return TopicBuilder.name("movement-rejected")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
