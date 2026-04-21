package com.ft.notification.infrastructure.sender;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationType;
import com.ft.notification.domain.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 이메일 발송 구현체 — Observer 구독자.
 * 현재는 Stub 구현으로 실제 SMTP/SES 연동 없이 로그만 출력한다.
 * 실제 연동 시 JavaMailSender 또는 AWS SES SDK 를 사용한다.
 */
@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public boolean send(Long userId, NotificationType type, String title, String message) {
        // TODO: JavaMailSender
        log.info("[Email Stub] 메일 발송 — userId={}, type={}, title={}", userId, type, title);
        return true;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }
}
