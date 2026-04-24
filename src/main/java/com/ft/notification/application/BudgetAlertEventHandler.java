package com.ft.notification.application;

import com.ft.common.event.BudgetAlertEvent;
import com.ft.common.kafka.EventHandler;
import com.ft.common.kafka.KafkaTopic;
import com.ft.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

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
        String title = buildTitle(event);
        String message = buildMessage(event);

        notificationService.send(event.userId(), type, title, message);
    }

    private NotificationType resolveNotificationType(String alertType) {
        return "EXCEEDED_100".equals(alertType) ? NotificationType.BUDGET_EXCEEDED : NotificationType.BUDGET_WARNING;
    }

    private String buildTitle(BudgetAlertEvent event) {
        String budgetLabel = event.categoryName() != null ? event.categoryName() + " 예산" : "예산";
        return switch (event.alertType()) {
            case "WARNING_50" -> "[" + event.yearMonth() + "] " + budgetLabel + " 50% 사용";
            case "WARNING_80" -> "[" + event.yearMonth() + "] " + budgetLabel + " 80% 사용";
            case "EXCEEDED_100" -> "[" + event.yearMonth() + "] " + budgetLabel + " 초과!";
            default -> "[" + event.yearMonth() + "] 예산 알림";
        };
    }

    private String buildMessage(BudgetAlertEvent event) {
        String category = event.categoryName() != null ? event.categoryName() : "카테고리 " + event.categoryId();
        BigDecimal spent = event.spentAmount();
        BigDecimal limit = event.limitAmount();
        BigDecimal usageRate = event.usageRate();

        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" 카테고리 예산 현황\n");
        sb.append("예산 한도  : ").append(formatAmount(limit)).append("원\n");
        sb.append("현재 사용  : ").append(formatAmount(spent)).append("원 (").append(usageRate.toPlainString()).append("%)\n");

        if ("EXCEEDED_100".equals(event.alertType())) {
            BigDecimal exceeded = spent.subtract(limit);
            sb.append("초과 금액  : ").append(formatAmount(exceeded)).append("원");
        } else {
            BigDecimal remaining = limit.subtract(spent);
            sb.append("남은 예산  : ").append(formatAmount(remaining)).append("원");
        }

        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(amount);
    }
}
