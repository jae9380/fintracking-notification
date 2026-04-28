package com.ft.notification.infrastructure.sender;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationContext;
import com.ft.notification.domain.sender.NotificationSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FCM(Firebase Cloud Messaging) 발송 구현체 — Observer 구독자.
 * 실제 연동 시 FirebaseMessaging.getInstance().send(Message) 를 호출한다.
 * context.fcmToken()이 null이면 NotificationService에서 사전에 걸러지므로 여기서는 항상 유효하다.
 */
@Slf4j
@Component
public class FcmNotificationSender implements NotificationSender {

    @Override
    @CircuitBreaker(name = "fcmSender", fallbackMethod = "sendFallback")
    public boolean send(NotificationContext context) {
        // TODO: FirebaseMessaging SDK 연동
        // Message message = Message.builder()
        //     .setToken(context.fcmToken())
        //     .setNotification(Notification.builder()
        //         .setTitle(context.title())
        //         .setBody(context.message())
        //         .build())
        //     .build();
        // FirebaseMessaging.getInstance().send(message);
        log.info("[FCM Stub] 푸시 발송 — userId={}, token={}..., title={}",
                context.userId(),
                context.fcmToken() != null ? context.fcmToken().substring(0, Math.min(8, context.fcmToken().length())) : "null",
                context.title());
        return true;
    }

    // Circuit OPEN 또는 FCM 연동 실패 누적 시 호출
    private boolean sendFallback(NotificationContext context, Throwable t) {
        log.warn("[FCM][CircuitBreaker] FCM 발송 차단 — userId={}, reason={}",
                context.userId(), t.getMessage());
        return false;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.FCM;
    }
}
