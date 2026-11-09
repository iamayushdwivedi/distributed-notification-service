package com.ayush.notification.consumer;

import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationStatus;
import com.ayush.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.InOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import static org.mockito.Mockito.*;

class NotificationEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationEventConsumer notificationEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSkipAlreadySentNotification() {

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.SENT);

        when(notificationRepository.findById(6L))
                .thenReturn(Optional.of(notification));

        notificationEventConsumer.consumeNotificationEvent("6");

        verify(notificationRepository).findById(6L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void shouldProcessPendingNotificationSuccessfully() {

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.findById(7L))
                .thenReturn(Optional.of(notification));

        notificationEventConsumer.consumeNotificationEvent("7");

        verify(notificationRepository).findById(7L);

        verify(notificationRepository, times(2))
                .save(notification);

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );
    }
}