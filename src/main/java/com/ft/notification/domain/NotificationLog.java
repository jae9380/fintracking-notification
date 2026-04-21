package com.ft.notification.domain;

import com.ft.common.entity.BaseEntity;
import com.ft.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.ft.common.exception.ErrorCode.NOTIFICATION_NO_ACCESS;

@Entity
@Table(name = "notification_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean isSuccess;

    @Column(nullable = false)
    private boolean isRead;

    private NotificationLog(
            Long userId,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            boolean isSuccess
    ) {
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.sentAt = LocalDateTime.now();
        this.isSuccess = isSuccess;
        this.isRead = false;
    }

    public static NotificationLog create(
            Long userId,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            boolean isSuccess
    ) {
        return new NotificationLog(userId, type, channel, title, message, isSuccess);
    }

    public void validateOwner(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new CustomException(NOTIFICATION_NO_ACCESS);
        }
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
