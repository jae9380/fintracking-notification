package com.ft.notification.application.port;

import com.ft.notification.domain.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository {

    NotificationLog save(NotificationLog notificationLog);

    Optional<NotificationLog> findById(Long id);

    // isRead 필터링 지원 — null 이면 전체 조회
    Page<NotificationLog> findAllByUserId(Long userId, Boolean isRead, Pageable pageable);

    // 전체 읽음 처리를 위한 벌크 업데이트
    void markAllAsReadByUserId(Long userId);
}
