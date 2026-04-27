package com.ft.notification.application;

import com.ft.common.event.BatchReportEvent;
import com.ft.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchReportEventHandler 단위 테스트")
class BatchReportEventHandlerTest {

    @Mock NotificationService notificationService;

    @InjectMocks BatchReportEventHandler handler;

    @Nested
    @DisplayName("월간 리포트 이벤트 처리")
    class HandleBatchReport {

        @Test
        @DisplayName("성공 - 이벤트 수신 시 MONTHLY_REPORT 타입으로 알림 발송")
        void handle_whenBatchReportEvent_sendsMonthlyReportNotification() {
            // given
            BatchReportEvent event = new BatchReportEvent(
                    "event-id", 1L, "2026-04",
                    "[2026-04] 월간 리포트", "총 수입: 500,000원 / 총 지출: 300,000원");

            // when
            handler.handle(event);

            // then
            then(notificationService).should().send(
                    1L, NotificationType.MONTHLY_REPORT,
                    "[2026-04] 월간 리포트", "총 수입: 500,000원 / 총 지출: 300,000원");
        }
    }
}
