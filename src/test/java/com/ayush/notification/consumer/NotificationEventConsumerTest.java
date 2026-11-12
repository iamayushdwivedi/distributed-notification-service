package com.ayush.notification.consumer;

import com.ayush.notification.entity.Notification;
import com.ayush.notification.enums.NotificationStatus;
import com.ayush.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import com.ayush.notification.email.BrevoEmailService;
import java.util.Optional;

import static org.mockito.Mockito.*;

class NotificationEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private BrevoEmailService brevoEmailService;

    private NotificationEventConsumer notificationEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        notificationEventConsumer = new NotificationEventConsumer(
                notificationRepository,
                cacheManager,
                brevoEmailService
        );
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

        when(cacheManager.getCache("notifications"))
                .thenReturn(cache);

        notificationEventConsumer.consumeNotificationEvent("7");

        verify(brevoEmailService).sendEmail(
                notification.getRecipient(),
                "NotifyFlow - New Notification",
                notification.getMessage()
        );

        verify(cacheManager).getCache("notifications");

        verify(cache).evict(7L);

        verify(notificationRepository).findById(7L);

        verify(notificationRepository, times(2))
                .save(notification);

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );
    }

    @Test
    void shouldThrowExceptionWhenDeliveryFails() {

        // Arrange: create a pending notification
        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRecipient("retry-test@example.com");
        notification.setMessage("Testing Brevo delivery failure");

        when(notificationRepository.findById(8L))
                .thenReturn(Optional.of(notification));

        // Simulate Brevo API failure
        doThrow(new RuntimeException("Brevo API unavailable"))
                .when(brevoEmailService)
                .sendEmail(
                        eq("retry-test@example.com"),
                        eq("NotifyFlow - New Notification"),
                        eq("Testing Brevo delivery failure")
                );

        // Act and assert: processing must fail
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationEventConsumer.consumeNotificationEvent("8")
        );

        assertEquals(
                "Brevo API unavailable",
                exception.getMessage()
        );

        // Notification was saved as PROCESSING before delivery failed
        verify(notificationRepository, times(1)).save(notification);

        // Brevo was called
        verify(brevoEmailService).sendEmail(
                "retry-test@example.com",
                "NotifyFlow - New Notification",
                "Testing Brevo delivery failure"
        );

        // Notification must not be marked SENT
        assertEquals(
                NotificationStatus.PROCESSING,
                notification.getStatus()
        );

        // Cache should not be evicted on this failure path
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldSkipFailedNotification() {

        // Arrange: notification has already failed
        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.FAILED);

        when(notificationRepository.findById(9L))
                .thenReturn(Optional.of(notification));

        // Act: simulate Kafka receiving the event again
        notificationEventConsumer.consumeNotificationEvent("9");

        // Assert: notification was fetched
        verify(notificationRepository).findById(9L);

        // No database update should happen
        verify(notificationRepository, never())
                .save(any(Notification.class));

        // No email should be sent
        verifyNoInteractions(brevoEmailService);

        // No cache changes should happen
        verifyNoInteractions(cacheManager);

        // Status remains FAILED
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
    }
}