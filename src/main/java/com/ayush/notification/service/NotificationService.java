package com.ayush.notification.service;

import com.ayush.notification.producer.NotificationEventProducer;
import com.ayush.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import com.ayush.notification.dto.NotificationRequest;
import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationChannel;
import com.ayush.notification.enums.NotificationStatus;
import com.ayush.notification.dto.NotificationResponse;
import com.ayush.notification.exception.InvalidNotificationChannelException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Locale;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventProducer notificationEventProducer;

    public NotificationService(NotificationRepository notificationRepository, NotificationEventProducer notificationEventProducer){
        this.notificationRepository = notificationRepository;
        this.notificationEventProducer = notificationEventProducer;
    }

    public String getServiceStatus() {
        return "Notification Service is running!";
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification();

        notification.setRecipient(request.getRecipient());
        notification.setMessage(request.getMessage());
        try {
            notification.setChannel(
                    NotificationChannel.valueOf(
                            request.getChannel().trim().toUpperCase(Locale.ROOT)
                    )
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidNotificationChannelException(
                    "Invalid notification channel: " + request.getChannel()
            );
        }
        notification.setStatus(NotificationStatus.PENDING);

        // Save entity to database
        Notification savedNotification = notificationRepository.save(notification);

        notificationEventProducer.publishNotificationEvent(savedNotification.getId());

        // Map entity to response DTO
        return convertToResponse(savedNotification);
    }

    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAll(pageable);

        return notifications.map(this::convertToResponse);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();

        response.setId(notification.getId());
        response.setRecipient(notification.getRecipient());
        response.setMessage(notification.getMessage());
        response.setChannel(notification.getChannel());
        response.setStatus(notification.getStatus());
        response.setCreatedAt(notification.getCreatedAt());

        return response;
    }
}