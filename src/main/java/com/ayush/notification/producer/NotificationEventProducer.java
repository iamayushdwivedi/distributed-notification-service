package com.ayush.notification.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventProducer {

    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationEventProducer(
            KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishNotificationEvent(Long notificationId) {
        kafkaTemplate.send(TOPIC, notificationId.toString());
    }
}
