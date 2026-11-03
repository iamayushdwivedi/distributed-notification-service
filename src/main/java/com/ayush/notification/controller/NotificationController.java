package com.ayush.notification.controller;

import com.ayush.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ayush.notification.dto.NotificationRequest;
import com.ayush.notification.entity.Notification;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return notificationService.getServiceStatus();
    }

    @PostMapping
    public Notification createNotification(
            @Valid @RequestBody NotificationRequest request) {
        return notificationService.createNotification(request);
    }
}