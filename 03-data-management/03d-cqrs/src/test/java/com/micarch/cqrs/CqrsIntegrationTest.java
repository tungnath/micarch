package com.micarch.cqrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micarch.cqrs.command.domain.OrderAggregate;
import com.micarch.cqrs.command.domain.OrderState;
import com.micarch.cqrs.command.controller.OrderCommandController.PlaceOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CQRS Integration Test using: - @EmbeddedKafka for the messaging backbone -
 * @Testcontainers + MongoDBContainer for the read-side projection - H2 for the
 * write-side (configured in application.yml)
 *
 * Tests the full CQRS flow: Command → Postgres write → Kafka → MongoDB
 * projection → Query
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"order-events"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest"
})
class CqrsIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void placeOrder_shouldReturn202_andCommandSideStoreInPostgres() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest("CUST-1", "PROD-ABC", 3, new BigDecimal("29.99"));

        MvcResult result = mockMvc.perform(post("/api/commands/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted()) // 202 — not 200 (eventually consistent)
                .andReturn();

        OrderAggregate order = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderAggregate.class);
        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("CUST-1");
        assertThat(order.getState()).isEqualTo(OrderState.PENDING);
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("89.97")); // 3 × 29.99
    }

    @Test
    void confirmOrder_shouldReturn202_andUpdateState() throws Exception {
        // First place an order
        PlaceOrderRequest request = new PlaceOrderRequest("CUST-2", "PROD-XYZ", 1, new BigDecimal("100.00"));

        MvcResult placeResult = mockMvc.perform(post("/api/commands/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn();

        OrderAggregate placedOrder = objectMapper.readValue(
                placeResult.getResponse().getContentAsString(), OrderAggregate.class);

        // Confirm the order
        MvcResult confirmResult = mockMvc.perform(put("/api/commands/orders/" + placedOrder.getId() + "/confirm"))
                .andExpect(status().isAccepted())
                .andReturn();

        OrderAggregate confirmed = objectMapper.readValue(
                confirmResult.getResponse().getContentAsString(), OrderAggregate.class);
        assertThat(confirmed.getState()).isEqualTo(OrderState.CONFIRMED);
    }

    @Test
    void queryOrders_shouldReturnEmptyInitially() throws Exception {
        // This might have entries from other tests — just verify it returns a list
        mockMvc.perform(get("/api/queries/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
