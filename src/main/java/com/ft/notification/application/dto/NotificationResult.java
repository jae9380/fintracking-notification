package com.ft.notification.application.dto;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationLog;
import com.ft.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResult(
        Long id,
        Long userId,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,
        LocalDateTime sentAt,
        boolean isSuccess,
        boolean isRead
) {
    public static NotificationResult from(NotificationLog log) {
        return new NotificationResult(
                log.getId(),
                log.getUserId(),
                log.getType(),
                log.getChannel(),
                log.getTitle(),
                log.getMessage(),
                log.getSentAt(),
                log.isSuccess(),
                log.isRead()
        );
    }
}
