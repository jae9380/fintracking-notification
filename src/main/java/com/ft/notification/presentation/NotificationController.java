package com.ft.notification.presentation;

import com.ft.common.response.ApiResponse;
import com.ft.notification.application.NotificationService;
import com.ft.notification.presentation.dto.NotificationResponse;
import com.ft.notification.presentation.dto.NotificationSettingsRequest;
import com.ft.notification.presentation.dto.NotificationSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회 (페이지네이션)")
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> findAll(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<NotificationResponse> responses = notificationService.findAll(userId, read, pageable)
                .map(NotificationResponse::from);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "알림 단건 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long notificationId
    ) {
        return ApiResponse.success(
                NotificationResponse.from(notificationService.markAsRead(userId, notificationId)));
    }

    @Operation(summary = "알림 전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.noContent();
    }

    @Operation(summary = "알림 설정 저장 (FCM / 이메일 활성화)")
    @PostMapping("/settings")
    public ApiResponse<NotificationSettingsResponse> updateSettings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody NotificationSettingsRequest request
    ) {
        NotificationSettingsResponse response = NotificationSettingsResponse.from(
                notificationService.updateSettings(userId, request.fcmEnabled(), request.emailEnabled(), request.email(), request.fcmToken())
        );
        return ApiResponse.success(response);
    }
}
