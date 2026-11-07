package com.ayush.notification.controller;

import com.ayush.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ayush.notification.dto.NotificationRequest;
import com.ayush.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.domain.Sort;

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

    @GetMapping
    public Page<NotificationResponse> getAllNotifications(
            @PageableDefault
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            })
            Pageable pageable) {

        return notificationService.getAllNotifications(pageable);
    }

    @PostMapping
    public NotificationResponse createNotification(
            @Valid @RequestBody NotificationRequest request) {
        return notificationService.createNotification(request);
    }
}