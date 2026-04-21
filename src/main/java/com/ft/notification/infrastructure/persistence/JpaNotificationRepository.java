package com.ft.notification.infrastructure.persistence;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaNotificationRepository extends JpaRepository<NotificationLog, Long> {

    // IN_APP 채널만 조회 (앱 내 알림함)
    Page<NotificationLog> findAllByUserIdAndChannel(Long userId, NotificationChannel channel, Pageable pageable);

    Page<NotificationLog> findAllByUserIdAndChannelAndIsRead(Long userId, NotificationChannel channel, boolean isRead, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationLog n SET n.isRead = true WHERE n.userId = :userId AND n.channel = 'IN_APP' AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
