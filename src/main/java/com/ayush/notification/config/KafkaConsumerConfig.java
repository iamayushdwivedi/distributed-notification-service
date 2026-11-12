package com.ayush.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.TopicPartition;
import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationStatus;
import com.ayush.notification.repository.NotificationRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;


@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            NotificationRepository notificationRepository,
            CacheManager cacheManager) {

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

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {

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

            // Remove any outdated notification response from Redis.
            Cache cache = cacheManager.getCache("notifications");

            if (cache != null) {
                cache.evict(notificationId);
            }

            System.out.println("Notification permanently failed: " + notificationId);

        }, backOff);

        // Permanent HTTP errors should go directly to the DLT.
        errorHandler.addNotRetryableExceptions(
                HttpClientErrorException.BadRequest.class,
                HttpClientErrorException.Unauthorized.class,
                HttpClientErrorException.Forbidden.class,
                HttpClientErrorException.NotFound.class,
                HttpClientErrorException.UnprocessableEntity.class
        );

        return errorHandler;
    }
}