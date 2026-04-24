package com.ft.notification.application.dto;

public record NotificationSettingsResult(
        Long userId,
        boolean fcmEnabled,
        boolean emailEnabled,
        String email,
        String fcmToken
) {
    public static NotificationSettingsResult of(Long userId, boolean fcmEnabled, boolean emailEnabled, String email, String fcmToken) {
        return new NotificationSettingsResult(userId, fcmEnabled, emailEnabled, email, fcmToken);
    }
}
