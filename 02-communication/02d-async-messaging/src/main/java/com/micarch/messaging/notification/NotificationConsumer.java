package com.micarch.messaging.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.micarch.messaging.order.OrderCreatedEvent;

/**
 * Notification consumer in a DIFFERENT consumer group
 * ('notification-service-group'). Both InventoryConsumer and
 * NotificationConsumer receive EVERY order event because they are in different
 * consumer groups — this demonstrates Kafka's publish-subscribe fan-out
 * capability.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "order-events", groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderCreatedEvent event) {
        log.info("NOTIFICATION: Sending order confirmation email to customerId={} for orderId={}",
                event.customerId(), event.orderId());

        // Simulate sending an email / push notification
        sendEmail(event.customerId(), event.orderId(), event.unitPrice());
    }

    private void sendEmail(String customerId, String orderId, java.math.BigDecimal unitPrice) {
        // In production: integrate with SendGrid, SES, or a notification microservice
        log.info("NOTIFICATION: ✅ Confirmation sent — Order {} for customer {} (unit price: {})",
                orderId, customerId, unitPrice);
    }
}
