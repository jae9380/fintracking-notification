package com.ft.notification.infrastructure.sender;

import com.ft.common.exception.CustomException;
import com.ft.notification.domain.NotificationChannel;
import com.ft.notification.domain.NotificationContext;
import com.ft.notification.domain.sender.NotificationSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.ft.common.exception.ErrorCode.NOTIFICATION_SEND_FAILED;

/**
 * 이메일 발송 구현체 — Observer 구독자.
 * Thymeleaf 템플릿(budget-alert-email)을 렌더링해 HTML 메일을 발송한다.
 * context.email()이 null이면 NotificationService에서 사전에 걸러지므로 여기서는 항상 유효하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private static final String EMAIL_TEMPLATE = "budget-alert-email";

    @Value("${spring.mail.username}")
    private String fromAddress;

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    public boolean send(NotificationContext notificationContext) {
        Context context = new Context();
        context.setVariable("title", notificationContext.title());
        context.setVariable("message", notificationContext.message());
        context.setVariable("type", notificationContext.type().name());

        String htmlContent = templateEngine.process(EMAIL_TEMPLATE, context);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setFrom("FinTracking <" + fromAddress + ">");
            helper.setTo(notificationContext.email());
            helper.setSubject("[FinTracking] " + notificationContext.title());
            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("[Email] 발송 실패 — userId={}, email={}, error={}",
                    notificationContext.userId(), notificationContext.email(), e.getMessage());
            throw new CustomException(NOTIFICATION_SEND_FAILED);
        }

        log.info("[Email] 발송 완료 — userId={}, email={}, title={}",
                notificationContext.userId(), notificationContext.email(), notificationContext.title());
        return true;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }
}
