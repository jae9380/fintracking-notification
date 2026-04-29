package com.ft.notification.infrastructure.sender;

import com.ft.common.metric.helper.ExternalApiMetricHelper;
import com.ft.notification.domain.NotificationContext;
import com.ft.notification.domain.NotificationType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FcmNotificationSenderTest {

    private MeterRegistry meterRegistry;
    private ExternalApiMetricHelper metricHelper;
    private FcmNotificationSender sut;

    private static final NotificationContext CONTEXT = new NotificationContext(
            1L,
            null,
            "fcm-device-token-abc123",
            NotificationType.BUDGET_WARNING,
            "예산 경고",
            "예산의 80%를 사용했습니다."
    );

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricHelper = new ExternalApiMetricHelper(meterRegistry);
        sut = new FcmNotificationSender(metricHelper);
    }

    @Test
    @DisplayName("send_성공_true반환_success카운터1증가")
    void send_success_returnsTrueAndIncrementsSuccessCounter() {
        // when
        boolean result = sut.send(CONTEXT);

        // then
        assertThat(result).isTrue();

        double successCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "fcm")
                .tag("operation", "send_push")
                .tag("result", "success")
                .counter().count();
        assertThat(successCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("send_성공_duration타이머기록")
    void send_success_recordsDuration() {
        // when
        sut.send(CONTEXT);

        // then
        long timerCount = meterRegistry.get("ft_external_api_duration_seconds")
                .tag("system", "fcm")
                .tag("operation", "send_push")
                .timer().count();
        assertThat(timerCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("send_여러번호출_success카운터누적")
    void send_multipleCalls_accumulatesSuccessCounter() {
        // when
        sut.send(CONTEXT);
        sut.send(CONTEXT);
        sut.send(CONTEXT);

        // then
        double count = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "fcm")
                .tag("result", "success")
                .counter().count();
        assertThat(count).isEqualTo(3.0);
    }

    @Test
    @DisplayName("send_fcmToken이_8자_미만이어도_정상처리")
    void send_shortFcmToken_handledSafely() {
        // given
        NotificationContext shortTokenContext = new NotificationContext(
                2L, null, "abc", NotificationType.BUDGET_EXCEEDED, "제목", "내용"
        );

        // when
        boolean result = sut.send(shortTokenContext);

        // then
        assertThat(result).isTrue();
    }
}
