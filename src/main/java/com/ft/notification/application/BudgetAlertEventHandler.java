package com.ft.notification.application;

import com.ft.common.event.BudgetAlertEvent;
import com.ft.common.kafka.EventHandler;
import com.ft.common.kafka.KafkaTopic;
import com.ft.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetAlertEventHandler implements EventHandler<BudgetAlertEvent> {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopic.BUDGET_ALERT, groupId = "notification-service")
    @Override
    public void handle(BudgetAlertEvent event) {
        log.info("[BudgetAlertEvent] 이벤트 수신 — userId={}, budgetId={}, alertType={}",
                event.userId(), event.budgetId(), event.alertType());

        NotificationType type = resolveNotificationType(event.alertType());
        String title = buildTitle(event.alertType());
        String message = buildMessage(event);

        notificationService.send(event.userId(), type, title, message);
    }

    private NotificationType resolveNotificationType(String alertType) {
        return "EXCEEDED_100".equals(alertType) ? NotificationType.BUDGET_EXCEEDED : NotificationType.BUDGET_WARNING;
    }

    private String buildTitle(String alertType) {
        return switch (alertType) {
            case "WARNING_50" -> "예산의 50%를 사용했습니다";
            case "WARNING_80" -> "예산의 80%를 사용했습니다";
            case "EXCEEDED_100" -> "예산을 초과했습니다";
            default -> "예산 알림";
        };
    }

    private String buildMessage(BudgetAlertEvent event) {
        return String.format("지출 금액: %s원 / 예산 한도: %s원",
                event.spentAmount().toPlainString(),
                event.limitAmount().toPlainString());
    }
}
