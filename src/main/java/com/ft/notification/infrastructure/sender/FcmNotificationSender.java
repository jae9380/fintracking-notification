package com.ft.notification.infrastructure.sender;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationType;
import com.ft.notification.domain.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FCM(Firebase Cloud Messaging) 발송 구현체 — Observer 구독자.
 * 현재는 Stub 구현으로 실제 FCM SDK 연동 없이 로그만 출력한다.
 * 실제 연동 시 FirebaseMessaging.getInstance().send(Message) 를 호출한다.
 */
@Slf4j
@Component
public class FcmNotificationSender implements NotificationSender {

    @Override
    public boolean send(Long userId, NotificationType type, String title, String message) {
        // TODO: FirebaseMessaging SDK 연동
        log.info("[FCM Stub] 푸시 발송 — userId={}, type={}, title={}", userId, type, title);
        return true;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.FCM;
    }
}
