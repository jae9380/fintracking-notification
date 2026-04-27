package com.ft.notification.infrastructure.persistence;

import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationLog;
import com.ft.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("JpaNotificationRepository 테스트")
class JpaNotificationRepositoryTest {

    @Autowired
    JpaNotificationRepository jpaNotificationRepository;

    private NotificationLog inApp(Long userId, boolean success) {
        return NotificationLog.create(userId, NotificationType.BUDGET_WARNING,
                NotificationChannel.IN_APP, "예산 알림", "내용", success);
    }

    private NotificationLog email(Long userId) {
        return NotificationLog.create(userId, NotificationType.BUDGET_WARNING,
                NotificationChannel.EMAIL, "예산 알림 이메일", "내용", true);
    }

    @Nested
    @DisplayName("채널별 알림 목록 조회")
    class FindAllByUserIdAndChannel {

        @Test
        @DisplayName("성공 - IN_APP 채널의 알림만 반환한다")
        void findAllByUserIdAndChannel_whenInApp_returnsOnlyInAppLogs() {
            // given
            jpaNotificationRepository.saveAll(List.of(
                    inApp(1L, true),
                    inApp(1L, true),
                    email(1L),
                    inApp(2L, true)  // 다른 유저
            ));

            // when
            Page<NotificationLog> page = jpaNotificationRepository.findAllByUserIdAndChannel(
                    1L, NotificationChannel.IN_APP, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent()).allMatch(l -> l.getChannel() == NotificationChannel.IN_APP);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 유저는 빈 목록을 반환한다")
        void findAllByUserIdAndChannel_whenUnknownUser_returnsEmpty() {
            // given
            jpaNotificationRepository.save(inApp(1L, true));

            // when
            Page<NotificationLog> page = jpaNotificationRepository.findAllByUserIdAndChannel(
                    999L, NotificationChannel.IN_APP, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("읽음 상태 필터링 조회")
    class FindAllByUserIdAndChannelAndIsRead {

        @Test
        @DisplayName("성공 - 읽음/안읽음 상태로 필터링된다")
        void findAllByUserIdAndChannelAndIsRead_whenFiltered_returnsByReadStatus() {
            // given
            NotificationLog unread = inApp(1L, true);
            NotificationLog read = inApp(1L, true);
            read.markAsRead();
            jpaNotificationRepository.saveAll(List.of(unread, read));

            // when
            Page<NotificationLog> unreadPage = jpaNotificationRepository
                    .findAllByUserIdAndChannelAndIsRead(1L, NotificationChannel.IN_APP, false, PageRequest.of(0, 10));
            Page<NotificationLog> readPage = jpaNotificationRepository
                    .findAllByUserIdAndChannelAndIsRead(1L, NotificationChannel.IN_APP, true, PageRequest.of(0, 10));

            // then
            assertThat(unreadPage.getContent()).hasSize(1);
            assertThat(readPage.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("전체 읽음 처리")
    class MarkAllAsReadByUserId {

        @Test
        @DisplayName("성공 - IN_APP 알림이 모두 읽음으로 변경된다")
        void markAllAsReadByUserId_whenCalled_updatesAllUnreadInApp() {
            // given
            jpaNotificationRepository.saveAll(List.of(
                    inApp(1L, true),
                    inApp(1L, true),
                    email(1L)  // EMAIL 채널 — 업데이트 대상 아님
            ));

            // when
            jpaNotificationRepository.markAllAsReadByUserId(1L);

            // then
            Page<NotificationLog> stillUnread = jpaNotificationRepository
                    .findAllByUserIdAndChannelAndIsRead(1L, NotificationChannel.IN_APP, false, PageRequest.of(0, 10));
            assertThat(stillUnread.getContent()).isEmpty();
        }
    }
}
