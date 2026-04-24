package com.ft.notification.presentation.dto;

import com.ft.notification.application.dto.NotificationSettingsResult;

public record NotificationSettingsResponse(
        boolean fcmEnabled,
        boolean emailEnabled,
        String email,
        String fcmToken
) {
    public static NotificationSettingsResponse from(NotificationSettingsResult result) {
        return new NotificationSettingsResponse(result.fcmEnabled(), result.emailEnabled(), result.email(), result.fcmToken());
    }
}
