package com.ft.notification.config;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Notification Service CircuitBreaker 설정 동작 검증 테스트.
 *
 * Resilience4JConfig 에 선언된 emailSender / fcmSender 설정값과 동일한
 * CircuitBreakerConfig를 직접 구성해 상태 전이 및 채널별 SlowCall 차이를 검증한다.
 *
 * Spring 컨텍스트 없이 Resilience4J API만 사용하므로 빠르게 실행된다.
 */
@DisplayName("Notification CircuitBreaker 설정 동작 검증")
class Resilience4JConfigTest {

    // ── 헬퍼 ─────────────────────────────────────────────────────────────

    private CircuitBreaker emailSenderCB() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slowCallDurationThreshold(Duration.ofSeconds(8))
                .slowCallRateThreshold(60)
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        return CircuitBreakerRegistry.of(config).circuitBreaker("emailSender");
    }

    private CircuitBreaker fcmSenderCB() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .slowCallRateThreshold(60)
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        return CircuitBreakerRegistry.of(config).circuitBreaker("fcmSender");
    }

    private void recordFailure(CircuitBreaker cb) {
        if (cb.tryAcquirePermission()) {
            cb.onError(0, TimeUnit.MILLISECONDS, new RuntimeException("external API error"));
        }
    }

    private void recordSuccess(CircuitBreaker cb) {
        if (cb.tryAcquirePermission()) {
            cb.onSuccess(0, TimeUnit.MILLISECONDS);
        }
    }

    private void recordCall(CircuitBreaker cb, long durationMs, boolean success) {
        if (cb.tryAcquirePermission()) {
            if (success) cb.onSuccess(durationMs, TimeUnit.MILLISECONDS);
            else cb.onError(durationMs, TimeUnit.MILLISECONDS, new RuntimeException("error"));
        }
    }

    // ── emailSender ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("emailSender — SMTP, slowCall=8s, waitDuration=30s")
    class EmailSender {

        @Test
        @DisplayName("초기 상태는 CLOSED")
        void initialState_isClosed() {
            // given
            CircuitBreaker cb = emailSenderCB();

            // when - 아무 호출 없음

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }

        @Test
        @DisplayName("5번 모두 실패 시 OPEN 전환 — 100% > 50%")
        void allFailures_opensCircuit() {
            // given
            CircuitBreaker cb = emailSenderCB();

            // when
            for (int i = 0; i < 5; i++) recordFailure(cb);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }

        @Test
        @DisplayName("3번 실패 + 2번 성공 = 60% > 50% → OPEN 전환")
        void threeFailuresTwoSuccesses_opensCircuit() {
            // given
            CircuitBreaker cb = emailSenderCB();

            // when
            for (int i = 0; i < 3; i++) recordFailure(cb);
            for (int i = 0; i < 2; i++) recordSuccess(cb);

            // then — 3/5 = 60% >= 50%
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }

        @Test
        @DisplayName("2번 실패 + 3번 성공 = 40% < 50% → CLOSED 유지")
        void twoFailuresThreeSuccesses_staysClosed() {
            // given
            CircuitBreaker cb = emailSenderCB();

            // when
            for (int i = 0; i < 2; i++) recordFailure(cb);
            for (int i = 0; i < 3; i++) recordSuccess(cb);

            // then — 2/5 = 40% < 50%
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }

        @Test
        @DisplayName("OPEN 상태에서 호출 시 CallNotPermittedException 발생")
        void openState_throwsCallNotPermittedException() {
            // given
            CircuitBreaker cb = emailSenderCB();
            cb.transitionToOpenState();

            // when & then
            assertThatThrownBy(() -> cb.executeSupplier(() -> "result"))
                    .isInstanceOf(CallNotPermittedException.class);
        }

        @Test
        @DisplayName("HALF_OPEN 2회 성공 시 CLOSED 복귀")
        void halfOpen_twoSuccesses_transitionsToClosed() {
            // given
            CircuitBreaker cb = emailSenderCB();
            cb.transitionToOpenState();
            cb.transitionToHalfOpenState();

            // when — permittedNumberOfCallsInHalfOpenState=2 전부 성공
            for (int i = 0; i < 2; i++) recordSuccess(cb);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }

        @Test
        @DisplayName("HALF_OPEN 실패 시 OPEN 재진입")
        void halfOpen_failure_transitionsBackToOpen() {
            // given
            CircuitBreaker cb = emailSenderCB();
            cb.transitionToOpenState();
            cb.transitionToHalfOpenState();

            // when
            for (int i = 0; i < 2; i++) recordFailure(cb);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }

        @Test
        @DisplayName("9s SlowCall 5회 = SlowCallRate 100% > 60% → OPEN 전환")
        void slowCalls_exceedingThreshold_opensCircuit() {
            // given
            CircuitBreaker cb = emailSenderCB();

            // when — 9000ms > 8s threshold → SlowCall
            for (int i = 0; i < 5; i++) recordCall(cb, 9000, true);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }

        @Test
        @DisplayName("4s 호출은 SlowCall 아님 — 8s 기준 미달로 CLOSED 유지")
        void fourSecondCall_isNotSlowForEmail_staysClosed() {
            // given
            CircuitBreaker cb = emailSenderCB();

            // when — 4000ms < 8s threshold → 정상 호출
            for (int i = 0; i < 5; i++) recordCall(cb, 4000, true);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }
    }

    // ── fcmSender ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("fcmSender — FCM HTTP/2, slowCall=3s, waitDuration=20s")
    class FcmSender {

        @Test
        @DisplayName("초기 상태는 CLOSED")
        void initialState_isClosed() {
            // given
            CircuitBreaker cb = fcmSenderCB();

            // when - 아무 호출 없음

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }

        @Test
        @DisplayName("5번 모두 실패 시 OPEN 전환")
        void allFailures_opensCircuit() {
            // given
            CircuitBreaker cb = fcmSenderCB();

            // when
            for (int i = 0; i < 5; i++) recordFailure(cb);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }

        @Test
        @DisplayName("4s SlowCall 5회 = SlowCallRate 100% > 60% → OPEN 전환")
        void slowCalls_exceedingThreshold_opensCircuit() {
            // given
            CircuitBreaker cb = fcmSenderCB();

            // when — 4000ms > 3s threshold → SlowCall
            for (int i = 0; i < 5; i++) recordCall(cb, 4000, true);

            // then
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }
    }

    // ── emailSender vs fcmSender 비교 ─────────────────────────────────────

    @Nested
    @DisplayName("emailSender vs fcmSender — SlowCall 임계값 차이 비교")
    class SlowCallThresholdComparison {

        @Test
        @DisplayName("4s 호출: emailSender(8s 기준)는 CLOSED, fcmSender(3s 기준)는 OPEN")
        void fourSecondCall_emailStaysClosed_fcmOpens() {
            // given
            CircuitBreaker emailCb = emailSenderCB();
            CircuitBreaker fcmCb = fcmSenderCB();

            // when — 4000ms: emailSender(정상), fcmSender(SlowCall)
            for (int i = 0; i < 5; i++) {
                recordCall(emailCb, 4000, true);
                recordCall(fcmCb, 4000, true);
            }

            // then
            assertThat(emailCb.getState()).isEqualTo(CircuitBreaker.State.CLOSED); // 4s < 8s → 정상
            assertThat(fcmCb.getState()).isEqualTo(CircuitBreaker.State.OPEN);    // 4s > 3s → SlowCall → OPEN
        }
    }
}
