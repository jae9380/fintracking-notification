# fintracking-notification

알림 발송 서비스 — Strategy + Observer 패턴

---

## 패턴: Strategy (채널) + Observer (이벤트 수신)

```
NotificationService
  └── NotificationStrategy 인터페이스
        ├── FcmNotificationStrategy   — FCM 푸시
        ├── EmailNotificationStrategy — 이메일
        └── SlackNotificationStrategy — Slack
```

채널 추가 시 `NotificationStrategy` 구현체만 추가.

---

## Kafka Consumer

`BudgetAlertEventHandler` — `budget.alert` 토픽 구독

```java
// 이벤트 → 알림 타입 매핑
"EXCEEDED_100" → NotificationType.BUDGET_EXCEEDED
그 외           → NotificationType.BUDGET_WARNING

notificationService.send(userId, type, title, message)
```

- `groupId: notification-service`

---

## NotificationType

```java
BUDGET_EXCEEDED  — 예산 100% 초과
BUDGET_WARNING   — 예산 임계값 경고 (50%, 80%)
TRANSACTION_ALERT — 거래 알림 (확장 예정)
```

---

## 알림 발송 흐름

```
BudgetAlertEvent 수신
  → NotificationType 결정
  → NotificationService.send()
  → 활성화된 모든 채널 Strategy에 위임
  → Notification 엔티티 저장 (이력)
```

---

## 패키지 구조

```
com.ft.notification
  ├── domain/          — Notification 엔티티, NotificationType
  ├── application/     — NotificationService, BudgetAlertEventHandler
  ├── infrastructure/  — FCM, 이메일, Slack 클라이언트
  └── presentation/    — NotificationController, DTO
```

---

## 주요 ErrorCode

```java
NOTIFICATION_SEND_FAILED(500, "NOTIFICATION_001", "Failed to send notification")
```
