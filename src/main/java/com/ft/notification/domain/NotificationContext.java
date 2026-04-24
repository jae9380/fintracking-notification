package com.ft.notification.domain;

/**
 * 알림 발송에 필요한 모든 정보를 담는 VO.
 * NotificationService가 NotificationSettings를 조회해 빌드한 뒤
 * 각 NotificationSender에 전달한다.
 */
public record NotificationContext(
        Long userId,
        String email,       // EmailNotificationSender 사용
        String fcmToken,    // FcmNotificationSender 사용
        NotificationType type,
        String title,
        String message
) {}
