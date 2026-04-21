package com.ft.notification.presentation.dto;

import com.ft.notification.application.dto.NotificationSettingsResult;

public record NotificationSettingsResponse(
        boolean fcmEnabled,
        boolean emailEnabled
) {
    public static NotificationSettingsResponse from(NotificationSettingsResult result) {
        return new NotificationSettingsResponse(result.fcmEnabled(), result.emailEnabled());
    }
}
