package com.ayush.notification.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.Mockito.mock;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.backoff.FixedBackOff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KafkaConsumerConfigTest {

    @Test
    void shouldNotRetryBadRequest() {
        AtomicInteger recoveryCount = new AtomicInteger();

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> recoveryCount.incrementAndGet(),
                new FixedBackOff(2000L, 2L)
        );

        errorHandler.addNotRetryableExceptions(
                HttpClientErrorException.BadRequest.class
        );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "notification-events", 0, 0L, "key", "9"
                );

        @SuppressWarnings("unchecked")
        Consumer<String, String> consumer = mock(Consumer.class);

        boolean handled = errorHandler.handleOne(
                new ListenerExecutionFailedException(
                        "Brevo API request failed",
                        HttpClientErrorException.create(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                HttpHeaders.EMPTY,
                                new byte[0],
                                null
                        )
                ),
                record,
                consumer,
                mock(MessageListenerContainer.class)
        );

        assertTrue(handled);
        assertEquals(1, recoveryCount.get());
    }

    @Test
    void shouldRetryInternalServerError() {
        AtomicInteger recoveryCount = new AtomicInteger();

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> recoveryCount.incrementAndGet(),
                new FixedBackOff(2000L, 2L)
        );

        errorHandler.addNotRetryableExceptions(
                HttpClientErrorException.BadRequest.class
        );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "notification-events", 0, 0L, "key", "9"
                );

        @SuppressWarnings("unchecked")
        Consumer<String, String> consumer = mock(Consumer.class);

        boolean handled = errorHandler.handleOne(
                new ListenerExecutionFailedException(
                        "Brevo server error",
                        new org.springframework.web.client.HttpServerErrorException(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                ),
                record,
                consumer,
                mock(MessageListenerContainer.class)
        );

        assertFalse(handled);
        assertEquals(0, recoveryCount.get());
    }
}