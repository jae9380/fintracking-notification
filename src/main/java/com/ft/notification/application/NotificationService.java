package com.ft.notification.application;

import com.ft.common.exception.CustomException;
import com.ft.common.metric.annotation.Monitored;
import com.ft.notification.application.dto.NotificationResult;
import com.ft.notification.application.dto.NotificationSettingsResult;
import com.ft.notification.application.port.NotificationRepository;
import com.ft.notification.application.port.NotificationSettingsRepository;
import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationContext;
import com.ft.notification.domain.NotificationLog;
import com.ft.notification.domain.NotificationSettings;
import com.ft.notification.domain.NotificationType;
import com.ft.notification.domain.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ft.common.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final Map<NotificationChannel, NotificationSender> senderMap;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationSettingsRepository notificationSettingsRepository,
            List<NotificationSender> senders
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationSettingsRepository = notificationSettingsRepository;
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(NotificationSender::channel, Function.identity()));
    }

    // 알림 목록 조회 (페이지네이션, 읽음 여부 필터)
    @Monitored(domain = "notification", layer = "service", api = "find_all")
    @Transactional(readOnly = true)
    public Page<NotificationResult> findAll(Long userId, Boolean isRead, Pageable pageable) {
        return notificationRepository.findAllByUserId(userId, isRead, pageable)
                .map(NotificationResult::from);
    }

    // 단건 읽음 처리
    @Monitored(domain = "notification", layer = "service", api = "mark_as_read")
    @Transactional
    public NotificationResult markAsRead(Long userId, Long notificationId) {
        NotificationLog notificationLog = getNotificationLog(notificationId);
        notificationLog.validateOwner(userId);
        notificationLog.markAsRead();
        return NotificationResult.from(notificationLog);
    }

    // 전체 읽음 처리
    @Monitored(domain = "notification", layer = "service", api = "mark_all_as_read")
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // 알림 발송
    @Monitored(domain = "notification", layer = "service", api = "send")
    @Transactional
    public void send(Long userId, NotificationType type, String title, String message) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId).orElse(null);

        NotificationContext context = new NotificationContext(
                userId,
                settings != null ? settings.getEmail() : null,
                settings != null ? settings.getFcmToken() : null,
                type,
                title,
                message
        );

        senderMap.forEach((channel, sender) -> {
            if (!isChannelEnabled(channel, settings)) {
                log.debug("[Notification] 채널 비활성 스킵 — channel={}, userId={}", channel, userId);
                return;
            }

            boolean success = false;
            try {
                success = sender.send(context);
            } catch (Exception e) {
                log.error("[Notification] 발송 실패 — channel={}, userId={}, error={}",
                        channel, userId, e.getMessage());
            }

            notificationRepository.save(NotificationLog.create(userId, type, channel, title, message, success));
            log.info("[Notification] 발송 완료 — channel={}, userId={}, type={}, success={}",
                    channel, userId, type, success);
        });
    }

    // 알림 설정 저장/수정
    @Monitored(domain = "notification", layer = "service", api = "update_settings")
    @Transactional
    public NotificationSettingsResult updateSettings(
            Long userId, boolean fcmEnabled, boolean emailEnabled, String email, String fcmToken) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .map(existing -> {
                    existing.update(fcmEnabled, emailEnabled, email, fcmToken);
                    return existing;
                })
                .orElse(NotificationSettings.create(userId, fcmEnabled, emailEnabled, email, fcmToken));

        NotificationSettings saved = notificationSettingsRepository.save(settings);
        log.info("[Notification] 설정 저장 — userId={}, fcm={}, email={}", userId, fcmEnabled, emailEnabled);
        return NotificationSettingsResult.of(saved.getUserId(), saved.isFcmEnabled(), saved.isEmailEnabled(), saved.getEmail(), saved.getFcmToken());
    }

    /**
     * IN_APP은 항상 활성, EMAIL/FCM은 사용자 설정에 따라 판단.
     * 설정이 없으면 IN_APP만 발송.
     */
    private boolean isChannelEnabled(NotificationChannel channel, NotificationSettings settings) {
        return switch (channel) {
            case IN_APP -> true;
            case EMAIL -> settings != null && settings.isEmailEnabled() && settings.getEmail() != null;
            case FCM -> settings != null && settings.isFcmEnabled() && settings.getFcmToken() != null;
        };
    }

    private NotificationLog getNotificationLog(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));
    }
}
