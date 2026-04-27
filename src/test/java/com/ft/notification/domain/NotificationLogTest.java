package com.ft.notification.domain;

import com.ft.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ft.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationLog 도메인 테스트")
class NotificationLogTest {

    @Nested
    @DisplayName("알림 로그 생성")
    class Create {

        @Test
        @DisplayName("성공 - 생성 직후 읽음 상태는 false이다")
        void success_validParams_initiallyUnread() {
            // given
            Long userId = 1L;
            NotificationType type = NotificationType.BUDGET_WARNING;
            NotificationChannel channel = NotificationChannel.IN_APP;

            // when
            NotificationLog log = NotificationLog.create(userId, type, channel, "예산 알림", "50% 사용", true);

            // then
            assertThat(log.getUserId()).isEqualTo(1L);
            assertThat(log.getType()).isEqualTo(NotificationType.BUDGET_WARNING);
            assertThat(log.getChannel()).isEqualTo(NotificationChannel.IN_APP);
            assertThat(log.getTitle()).isEqualTo("예산 알림");
            assertThat(log.isSuccess()).isTrue();
            assertThat(log.isRead()).isFalse();
            assertThat(log.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 발송 실패 시 success가 false로 기록된다")
        void success_failedSend_recordsFailure() {
            // when
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.BUDGET_EXCEEDED, NotificationChannel.EMAIL,
                    "예산 초과", "초과 메시지", false);

            // then
            assertThat(log.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("소유자 검증")
    class ValidateOwner {

        @Test
        @DisplayName("성공 - 본인이면 예외 없음")
        void success_sameUser_noException() {
            // given
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.MONTHLY_REPORT, NotificationChannel.IN_APP,
                    "월간 리포트", "내용", true);

            // when & then
            assertThatNoException().isThrownBy(() -> log.validateOwner(1L));
        }

        @Test
        @DisplayName("실패 - 다른 사용자이면 예외 발생")
        void fail_differentUser_throwsException() {
            // given
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.MONTHLY_REPORT, NotificationChannel.IN_APP,
                    "월간 리포트", "내용", true);

            // when & then
            assertThatThrownBy(() -> log.validateOwner(999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(NOTIFICATION_NO_ACCESS));
        }
    }

    @Nested
    @DisplayName("읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("성공 - 읽음 처리 후 isRead가 true가 된다")
        void success_unreadLog_becomesRead() {
            // given
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.BUDGET_WARNING, NotificationChannel.IN_APP,
                    "예산 알림", "메시지", true);
            assertThat(log.isRead()).isFalse();

            // when
            log.markAsRead();

            // then
            assertThat(log.isRead()).isTrue();
        }
    }
}
