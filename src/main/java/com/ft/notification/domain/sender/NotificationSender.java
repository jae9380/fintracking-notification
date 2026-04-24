package com.ft.notification.domain.sender;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationContext;

/**
 * Observer 패턴 — 알림 채널 추상화 인터페이스.
 * 새 채널(Slack, SMS 등)은 이 인터페이스를 구현하고 NotificationService에 등록한다.
 */
public interface NotificationSender {

    /**
     * 알림을 실제 채널(FCM / Email 등)로 발송한다.
     * NotificationContext에서 채널별로 필요한 정보(email, fcmToken 등)를 꺼내 사용한다.
     *
     * @return 발송 성공 여부
     */
    boolean send(NotificationContext context);

    /**
     * 이 Sender가 담당하는 채널 종류를 반환한다.
     * NotificationService가 채널별 Sender를 구분하는 데 사용한다.
     */
    NotificationChannel channel();
}
