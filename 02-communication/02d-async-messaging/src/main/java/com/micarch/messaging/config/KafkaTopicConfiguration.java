package com.micarch.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Creates the application topics when the broker allows application startup to manage topics. */
@Configuration
public class KafkaTopicConfiguration {

    @Bean
    NewTopic orderCreatedTopic(@Value("${kafka.topics.order-created}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic inventoryTopic(@Value("${kafka.topics.inventory-reserved}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic notificationTopic(@Value("${kafka.topics.notification}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
