package com.ft.notification.infrastructure.persistence;

import com.ft.notification.application.port.NotificationRepository;
import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        return jpaNotificationRepository.save(notificationLog);
    }

    @Override
    public Optional<NotificationLog> findById(Long id) {
        return jpaNotificationRepository.findById(id);
    }

    // IN_APP 채널만 반환 — 앱 내 알림함 전용
    @Override
    public Page<NotificationLog> findAllByUserId(Long userId, Boolean isRead, Pageable pageable) {
        if (isRead == null) {
            return jpaNotificationRepository.findAllByUserIdAndChannel(userId, NotificationChannel.IN_APP, pageable);
        }
        return jpaNotificationRepository.findAllByUserIdAndChannelAndIsRead(userId, NotificationChannel.IN_APP, isRead, pageable);
    }

    @Override
    public void markAllAsReadByUserId(Long userId) {
        jpaNotificationRepository.markAllAsReadByUserId(userId);
    }
}
