package com.ft.notification.domain;

import com.ft.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private boolean fcmEnabled;

    @Column(nullable = false)
    private boolean emailEnabled;

    private String email;      // 이메일 알림 수신 주소

    private String fcmToken;   // FCM 디바이스 토큰

    private NotificationSettings(Long userId, boolean fcmEnabled, boolean emailEnabled, String email, String fcmToken) {
        this.userId = userId;
        this.fcmEnabled = fcmEnabled;
        this.emailEnabled = emailEnabled;
        this.email = email;
        this.fcmToken = fcmToken;
    }

    public static NotificationSettings create(Long userId, boolean fcmEnabled, boolean emailEnabled, String email, String fcmToken) {
        return new NotificationSettings(userId, fcmEnabled, emailEnabled, email, fcmToken);
    }

    public void update(boolean fcmEnabled, boolean emailEnabled, String email, String fcmToken) {
        this.fcmEnabled = fcmEnabled;
        this.emailEnabled = emailEnabled;
        this.email = email;
        this.fcmToken = fcmToken;
    }
}
