package com.ft.notification.application;

import com.ft.common.event.BatchReportEvent;
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
public class BatchReportEventHandler implements EventHandler<BatchReportEvent> {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopic.BATCH_REPORT, groupId = "notification-service")
    @Override
    public void handle(BatchReportEvent event) {
        log.info("[BatchReportEvent] 이벤트 수신 — userId={}, yearMonth={}",
                event.userId(), event.yearMonth());

        notificationService.send(event.userId(), NotificationType.MONTHLY_REPORT, event.title(), event.message());
    }
}
