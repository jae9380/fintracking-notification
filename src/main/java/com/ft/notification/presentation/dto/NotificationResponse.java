package com.ft.notification.presentation.dto;

import com.ft.notification.application.dto.NotificationResult;
import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,
        LocalDateTime sentAt,
        boolean isSuccess,
        boolean isRead
) {
    public static NotificationResponse from(NotificationResult result) {
        return new NotificationResponse(
                result.id(),
                result.type(),
                result.channel(),
                result.title(),
                result.message(),
                result.sentAt(),
                result.isSuccess(),
                result.isRead()
        );
    }
}
