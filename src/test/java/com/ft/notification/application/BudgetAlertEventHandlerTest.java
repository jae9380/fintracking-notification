package com.ft.notification.application;

import com.ft.common.event.BudgetAlertEvent;
import com.ft.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetAlertEventHandler 단위 테스트")
class BudgetAlertEventHandlerTest {

    @Mock NotificationService notificationService;

    @InjectMocks BudgetAlertEventHandler handler;

    private BudgetAlertEvent event(String alertType) {
        return new BudgetAlertEvent(
                "event-id", 1L, 10L, 5L, "식비",
                alertType,
                new BigDecimal("50000"), new BigDecimal("100000"),
                new BigDecimal("50.00"), "2026-04"
        );
    }

    @Nested
    @DisplayName("경고 알림 처리")
    class WarningAlert {

        @Test
        @DisplayName("성공 - WARNING_50 이벤트 수신 시 BUDGET_WARNING 타입으로 알림 발송")
        void handle_whenWarning50_sendsWarningNotification() {
            // given
            BudgetAlertEvent warningEvent = event("WARNING_50");

            // when
            handler.handle(warningEvent);

            // then
            then(notificationService).should().send(
                    eq(1L), eq(NotificationType.BUDGET_WARNING),
                    startsWith("[2026-04]"), any());
        }

        @Test
        @DisplayName("성공 - WARNING_80 이벤트 수신 시 BUDGET_WARNING 타입으로 알림 발송")
        void handle_whenWarning80_sendsWarningNotification() {
            // given
            BudgetAlertEvent warningEvent = event("WARNING_80");

            // when
            handler.handle(warningEvent);

            // then
            then(notificationService).should().send(
                    eq(1L), eq(NotificationType.BUDGET_WARNING),
                    startsWith("[2026-04]"), any());
        }
    }

    @Nested
    @DisplayName("초과 알림 처리")
    class ExceededAlert {

        @Test
        @DisplayName("성공 - EXCEEDED_100 이벤트 수신 시 BUDGET_EXCEEDED 타입으로 알림 발송")
        void handle_whenExceeded100_sendsExceededNotification() {
            // given
            BudgetAlertEvent exceededEvent = new BudgetAlertEvent(
                    "event-id", 1L, 10L, 5L, "식비",
                    "EXCEEDED_100",
                    new BigDecimal("120000"), new BigDecimal("100000"),
                    new BigDecimal("120.00"), "2026-04"
            );

            // when
            handler.handle(exceededEvent);

            // then
            then(notificationService).should().send(
                    eq(1L), eq(NotificationType.BUDGET_EXCEEDED),
                    startsWith("[2026-04]"), any());
        }
    }
}
