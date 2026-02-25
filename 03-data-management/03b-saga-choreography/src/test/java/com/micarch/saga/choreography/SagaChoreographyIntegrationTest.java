package com.micarch.saga.choreography;

import com.micarch.saga.choreography.events.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Saga Choreography. Uses @EmbeddedKafka — no Docker
 * required for this test. Demonstrates the publish-subscribe choreography
 * pattern.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order-events", "inventory-events", "stock-failed-events"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest"
})
class SagaChoreographyIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void whenOrderCreatedEventPublished_thenInventoryConsumerReceivesIt() throws Exception {
        // Arrange — producer config
        Map<String, Object> producerConfig = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        KafkaTemplate<String, OrderCreatedEvent> template
                = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfig));

        // Arrange — consumer config (simulates InventoryEventHandler)
        Map<String, Object> consumerConfig = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        var consumer = new DefaultKafkaConsumerFactory<String, OrderCreatedEvent>(
                consumerConfig, new StringDeserializer(),
                new JsonDeserializer<>(OrderCreatedEvent.class)
        ).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "order-events");

        // Act — publish OrderCreatedEvent
        OrderCreatedEvent event = new OrderCreatedEvent(
                "ORD-001", "CUST-1", "PROD-1", 5, new BigDecimal("49.99")
        );
        template.send("order-events", "ORD-001", event);

        // Assert — consumer receives the event
        ConsumerRecord<String, OrderCreatedEvent> received
                = KafkaTestUtils.getSingleRecord(consumer, "order-events", 5000);

        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo("ORD-001");
        assertThat(received.value().orderId()).isEqualTo("ORD-001");
        assertThat(received.value().productId()).isEqualTo("PROD-1");
        assertThat(received.value().quantity()).isEqualTo(5);

        consumer.close();
    }
}
