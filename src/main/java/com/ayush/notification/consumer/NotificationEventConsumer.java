package com.ayush.notification.consumer;

import com.ayush.notification.entity.Notification;
import com.ayush.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.ayush.notification.enums.NotificationStatus;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import com.ayush.notification.email.BrevoEmailService;

@Component
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final CacheManager cacheManager;
    private final BrevoEmailService brevoEmailService;

    public NotificationEventConsumer(
            NotificationRepository notificationRepository,
            CacheManager cacheManager,
            BrevoEmailService brevoEmailService) {

        this.notificationRepository = notificationRepository;
        this.cacheManager = cacheManager;
        this.brevoEmailService = brevoEmailService;
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

        // Skip notifications that have already reached a terminal state
        if (notification.getStatus() == NotificationStatus.SENT
                || notification.getStatus() == NotificationStatus.FAILED) {

            System.out.println(
                    "Skipping notification " + id
                            + " because its status is " + notification.getStatus()
            );

            return;
        }

        // Mark notification as being processed
        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);

        brevoEmailService.sendEmail(
                notification.getRecipient(),
                "NotifyFlow - New Notification",
                notification.getMessage()
        );

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