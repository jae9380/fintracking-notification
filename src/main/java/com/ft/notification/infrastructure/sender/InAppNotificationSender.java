package com.ft.notification.infrastructure.sender;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationType;
import com.ft.notification.domain.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 서비스 내 알림 구현체 — Observer 구독자.
 * 실제 발송 없이 true를 반환하며, NotificationService가 notification_logs에
 * IN_APP 채널 레코드를 저장함으로써 앱 내 알림함이 구성된다.
 */
@Slf4j
@Component
public class InAppNotificationSender implements NotificationSender {

    @Override
    public boolean send(Long userId, NotificationType type, String title, String message) {
        log.info("[InApp] 알림 생성 — userId={}, type={}, title={}", userId, type, title);
        return true;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }
}
