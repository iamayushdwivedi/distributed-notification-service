package com.ayush.notification.consumer;

import com.ayush.notification.entity.Notification;
import com.ayush.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.ayush.notification.enums.NotificationStatus;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Component
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final CacheManager cacheManager;

    public NotificationEventConsumer(
            NotificationRepository notificationRepository,
            CacheManager cacheManager) {

        this.notificationRepository = notificationRepository;
        this.cacheManager = cacheManager;
    }

    @KafkaListener(topics = "notification-events")
    public void consumeNotificationEvent(String notificationId) {

        Long id = Long.parseLong(notificationId);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found: " + id
                        )
                );

        // Avoid processing notifications that are already sent
        if (notification.getStatus() == NotificationStatus.SENT) {
            System.out.println("Notification already sent: " + id);
            return;
        }

        // Mark notification as being processed
        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);

        System.out.println("Simulating delivery for notification: " + id);
        System.out.println("Recipient: " + notification.getRecipient());
        System.out.println("Channel: " + notification.getChannel());

        if ("retry-test@example.com".equals(notification.getRecipient())) {
            throw new RuntimeException(
                    "Simulated email delivery failure for notification: " + id
            );
        }

        // Simulate successful delivery
        notification.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification);

        // Remove any outdated notification response from Redis.
        Cache cache = cacheManager.getCache("notifications");

        if (cache != null) {
            cache.evict(id);
        }

        System.out.println("Notification sent successfully: " + id);
    }
}