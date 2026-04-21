package com.ft.notification.presentation.dto;

public record NotificationSettingsRequest(
        boolean fcmEnabled,
        boolean emailEnabled
) {
}
