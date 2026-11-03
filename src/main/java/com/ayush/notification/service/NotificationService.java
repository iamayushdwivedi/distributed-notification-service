package com.ayush.notification.service;

import com.ayush.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import com.ayush.notification.dto.NotificationRequest;
import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationChannel;
import com.ayush.notification.enums.NotificationStatus;
import java.util.Locale;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository){
        this.notificationRepository = notificationRepository;
    }

    public String getServiceStatus() {
        return "Notification Service is running!";
    }

    public Notification createNotification(NotificationRequest request) {
        Notification notification = new Notification();

        notification.setRecipient(request.getRecipient());
        notification.setMessage(request.getMessage());
        notification.setChannel(
                NotificationChannel.valueOf(
                        request.getChannel().trim().toUpperCase(Locale.ROOT)
                )
        );
        notification.setStatus(NotificationStatus.PENDING);

        return notificationRepository.save(notification);
    }
}