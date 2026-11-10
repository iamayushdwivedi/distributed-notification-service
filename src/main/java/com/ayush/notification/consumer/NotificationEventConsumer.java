package com.ayush.notification.consumer;

import com.ayush.notification.entity.Notification;
import com.ayush.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.ayush.notification.enums.NotificationStatus;

@Component
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;

    public NotificationEventConsumer(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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

        System.out.println("Notification sent successfully: " + id);
    }
}