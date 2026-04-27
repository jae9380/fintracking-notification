package com.ft.notification.application;

import com.ft.common.exception.CustomException;
import com.ft.common.exception.ErrorCode;
import com.ft.notification.application.dto.NotificationResult;
import com.ft.notification.application.port.NotificationRepository;
import com.ft.notification.application.port.NotificationSettingsRepository;
import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationContext;
import com.ft.notification.domain.NotificationLog;
import com.ft.notification.domain.NotificationSettings;
import com.ft.notification.domain.NotificationType;
import com.ft.notification.domain.sender.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationSettingsRepository notificationSettingsRepository;
    @Mock NotificationSender inAppSender;
    @Mock NotificationSender emailSender;

    NotificationService notificationService;

    @BeforeEach
    void setUp() {
        given(inAppSender.channel()).willReturn(NotificationChannel.IN_APP);
        given(emailSender.channel()).willReturn(NotificationChannel.EMAIL);
        notificationService = new NotificationService(
                notificationRepository, notificationSettingsRepository,
                List.of(inAppSender, emailSender));
    }

    @Nested
    @DisplayName("알림 발송")
    class Send {

        @Test
        @DisplayName("성공 - 설정이 없으면 IN_APP 채널만 발송된다")
        void send_whenNoSettings_onlyInAppSenderCalled() {
            // given
            given(notificationSettingsRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(inAppSender.send(any())).willReturn(true);
            given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            notificationService.send(1L, NotificationType.BUDGET_WARNING, "제목", "내용");

            // then
            then(inAppSender).should().send(any(NotificationContext.class));
            then(emailSender).should(never()).send(any());
        }

        @Test
        @DisplayName("성공 - 이메일 설정이 활성화되면 이메일 채널도 발송된다")
        void send_whenEmailEnabled_emailSenderCalled() {
            // given
            NotificationSettings settings = NotificationSettings.create(
                    1L, false, true, "user@example.com", null);
            given(notificationSettingsRepository.findByUserId(1L)).willReturn(Optional.of(settings));
            given(inAppSender.send(any())).willReturn(true);
            given(emailSender.send(any())).willReturn(true);
            given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            notificationService.send(1L, NotificationType.BUDGET_WARNING, "제목", "내용");

            // then
            then(emailSender).should().send(any(NotificationContext.class));
        }

        @Test
        @DisplayName("성공 - 이메일 설정은 활성화됐지만 주소가 없으면 이메일 발송 안 함")
        void send_whenEmailEnabledButNoAddress_emailSenderNotCalled() {
            // given
            NotificationSettings settings = NotificationSettings.create(
                    1L, false, true, null, null);
            given(notificationSettingsRepository.findByUserId(1L)).willReturn(Optional.of(settings));
            given(inAppSender.send(any())).willReturn(true);
            given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            notificationService.send(1L, NotificationType.BUDGET_WARNING, "제목", "내용");

            // then
            then(emailSender).should(never()).send(any());
        }

        @Test
        @DisplayName("성공 - 발송 중 예외가 발생하면 실패 로그를 저장한다")
        void send_whenSenderThrowsException_savesFailureLog() {
            // given
            given(notificationSettingsRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(inAppSender.send(any())).willThrow(new RuntimeException("발송 오류"));
            given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            notificationService.send(1L, NotificationType.BUDGET_WARNING, "제목", "내용");

            // then
            then(notificationRepository).should().save(argThat(log -> !log.isSuccess()));
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("성공 - 본인의 알림이면 읽음 처리된다")
        void markAsRead_whenValidOwner_returnsReadLog() {
            // given
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.BUDGET_WARNING, NotificationChannel.IN_APP, "제목", "내용", true);
            given(notificationRepository.findById(10L)).willReturn(Optional.of(log));

            // when
            NotificationResult result = notificationService.markAsRead(1L, 10L);

            // then
            assertThat(result.isRead()).isTrue();
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 알림이면 예외 발생")
        void markAsRead_whenWrongOwner_throwsCustomException() {
            // given
            NotificationLog log = NotificationLog.create(
                    1L, NotificationType.BUDGET_WARNING, NotificationChannel.IN_APP, "제목", "내용", true);
            given(notificationRepository.findById(10L)).willReturn(Optional.of(log));

            // when & then
            assertThatThrownBy(() -> notificationService.markAsRead(999L, 10L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NOTIFICATION_NO_ACCESS));
        }

        @Test
        @DisplayName("실패 - 알림이 존재하지 않으면 예외 발생")
        void markAsRead_whenNotFound_throwsCustomException() {
            // given
            given(notificationRepository.findById(10L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.markAsRead(1L, 10L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("전체 읽음 처리")
    class MarkAllAsRead {

        @Test
        @DisplayName("성공 - Repository의 일괄 업데이트를 호출한다")
        void markAllAsRead_whenCalled_callsRepositoryBulkUpdate() {
            // given
            Long userId = 1L;

            // when
            notificationService.markAllAsRead(userId);

            // then
            then(notificationRepository).should().markAllAsReadByUserId(userId);
        }
    }
}
