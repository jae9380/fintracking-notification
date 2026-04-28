package com.ft.notification.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Notification Service Circuit Breaker 설정 — Resilience4J (@CircuitBreaker AOP)
 *
 * CircuitBreakerConfigCustomizer 를 통해 외부 API 채널별로 개별 설정한다.
 * EmailNotificationSender, FcmNotificationSender 의 @CircuitBreaker 와 name으로 연결된다.
 *
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     Circuit Breaker 설정 항목 요약                            │
 * ├──────────────┬──────────────┬───────────┬─────────────┬──────────┬──────────┤
 * │   인스턴스    │slidingWindow │failureRate│waitDuration │ SlowCall │slowRate  │
 * ├──────────────┼──────────────┼───────────┼─────────────┼──────────┼──────────┤
 * │ emailSender  │      5       │   50 %    │    30 s     │   8 s    │  60 %    │
 * │ fcmSender    │      5       │   50 %    │    20 s     │   3 s    │  60 %    │
 * └──────────────┴──────────────┴───────────┴─────────────┴──────────┴──────────┘
 *
 * 설정 항목 설명:
 *   slidingWindowSize              : 실패율/SlowCall 계산 기준이 되는 최근 호출 수
 *   minimumNumberOfCalls           : 회로 평가 시작 최소 호출 수 (기본값 100 — 반드시 명시)
 *   failureRateThreshold           : 이 비율 이상 실패하면 OPEN 전환 (%)
 *   waitDurationInOpenState        : OPEN 유지 시간, 경과 후 HALF_OPEN으로 전환
 *   slowCallDurationThreshold      : 이 시간 초과 호출을 SlowCall로 분류
 *   slowCallRateThreshold          : SlowCall 비율이 이 값 이상이면 OPEN 전환 (%)
 *   permittedNumberOfCallsInHalfOpen: HALF_OPEN 상태에서 허용할 테스트 호출 수
 *
 * 채널별 설정 근거:
 *   emailSender — SMTP는 TLS 핸드쉐이크·DNS 조회로 본질적으로 느림(SlowCall 8s).
 *                 메일 서버 장애는 복구에 시간이 걸리므로 waitDuration을 30s로 설정.
 *   fcmSender   — FCM은 HTTP/2 REST API로 응답이 빠름(SlowCall 3s).
 *                 상대적으로 빠른 복구를 기대해 waitDuration을 20s로 설정.
 */
@Configuration
public class Resilience4JConfig {

    @Bean
    public CircuitBreakerConfigCustomizer emailSenderCustomizer() {
        return CircuitBreakerConfigCustomizer.of("emailSender", builder -> builder
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slowCallDurationThreshold(Duration.ofSeconds(8))
                .slowCallRateThreshold(60)
                .permittedNumberOfCallsInHalfOpenState(2)
        );
    }

    @Bean
    public CircuitBreakerConfigCustomizer fcmSenderCustomizer() {
        return CircuitBreakerConfigCustomizer.of("fcmSender", builder -> builder
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .slowCallRateThreshold(60)
                .permittedNumberOfCallsInHalfOpenState(2)
        );
    }
}
