package com.ayush.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationStatus;
import com.ayush.notification.repository.NotificationRepository;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            NotificationRepository notificationRepository) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        "notification-events.DLT",
                                        record.partition()
                                )
                );

        // Retry twice, waiting 2 seconds between attempts.
        FixedBackOff backOff = new FixedBackOff(2000L, 2L);

        return new DefaultErrorHandler((record, exception) -> {

            // First, publish the failed event to the Dead Letter Topic.
            recoverer.accept(record, exception);

            // Then update the corresponding notification in PostgreSQL.
            Long notificationId = Long.parseLong(record.value().toString());

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Notification not found: " + notificationId
                    ));

            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);

            System.out.println("Notification permanently failed: " + notificationId);

        }, backOff);
    }
}