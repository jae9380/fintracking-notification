package com.ft.notification.application.port;

import com.ft.notification.domain.NotificationSettings;

import java.util.Optional;

public interface NotificationSettingsRepository {
    NotificationSettings save(NotificationSettings settings);
    Optional<NotificationSettings> findByUserId(Long userId);
}
