package com.ayush.notification.service;

import com.ayush.notification.repository.NotificationRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ayush.notification.dto.NotificationRequest;
import com.ayush.notification.dto.NotificationResponse;
import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.verify;

import com.ayush.notification.exception.InvalidNotificationChannelException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldCreateNotificationWithPendingStatus() {

        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("test@example.com");
        request.setMessage("Test notification");
        request.setChannel("EMAIL");

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        NotificationResponse response =
                notificationService.createNotification(request);

        // Assert
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();

        assertEquals("test@example.com", savedNotification.getRecipient());
        assertEquals("Test notification", savedNotification.getMessage());
        assertEquals(NotificationStatus.PENDING, savedNotification.getStatus());
    }

    @Test
    void shouldRejectInvalidNotificationChannel() {

        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("test@example.com");
        request.setMessage("Test notification");
        request.setChannel("PUSH");

        // Act and Assert
        InvalidNotificationChannelException exception =
                assertThrows(
                        InvalidNotificationChannelException.class,
                        () -> notificationService.createNotification(request)
                );

        assertEquals(
                "Invalid notification channel: PUSH",
                exception.getMessage()
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }
}