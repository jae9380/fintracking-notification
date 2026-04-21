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

    private NotificationSettings(Long userId, boolean fcmEnabled, boolean emailEnabled) {
        this.userId = userId;
        this.fcmEnabled = fcmEnabled;
        this.emailEnabled = emailEnabled;
    }

    public static NotificationSettings create(Long userId, boolean fcmEnabled, boolean emailEnabled) {
        return new NotificationSettings(userId, fcmEnabled, emailEnabled);
    }

    public void update(boolean fcmEnabled, boolean emailEnabled) {
        this.fcmEnabled = fcmEnabled;
        this.emailEnabled = emailEnabled;
    }
}
