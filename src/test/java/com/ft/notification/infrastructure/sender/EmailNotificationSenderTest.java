package com.ft.notification.infrastructure.sender;

import com.ft.common.metric.helper.ExternalApiMetricHelper;
import com.ft.notification.domain.NotificationContext;
import com.ft.notification.domain.NotificationType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EmailNotificationSender 실제 발송 테스트.
 * 외부 서비스(Config Server, DB, Kafka, Eureka) 없이 단독 실행 가능.
 *
 * 실행 전 src/test/resources/email-test.properties 에 Gmail 계정 정보를 입력하세요.
 */
class EmailNotificationSenderTest {

    private EmailNotificationSender sender;
    private String recipientEmail;

    @BeforeEach
    void setUp() throws IOException {
        Properties props = loadEmailProperties();

        String username = props.getProperty("mail.username");
        String password = props.getProperty("mail.password");
        recipientEmail = props.getProperty("mail.recipient");

        // 설정이 없으면 테스트 스킵
        assumeTrue(!username.startsWith("YOUR_"), "email-test.properties에 실제 계정 정보를 입력하세요.");

        ExternalApiMetricHelper metricHelper = new ExternalApiMetricHelper(new SimpleMeterRegistry());
        sender = new EmailNotificationSender(buildMailSender(username, password), buildTemplateEngine(), metricHelper);
        ReflectionTestUtils.setField(sender, "fromAddress", username);
    }

    @Test
    @DisplayName("예산_50퍼센트_경고_이메일_발송")
    void send_warning50_sendEmail() {
        NotificationContext context = new NotificationContext(
                1L,
                recipientEmail,
                null,
                NotificationType.BUDGET_WARNING,
                "[2026-04] 식비 예산 50% 사용",
                "식비 카테고리 예산 현황\n" +
                "예산 한도  : 100,000원\n" +
                "현재 사용  : 50,000원 (50.00%)\n" +
                "남은 예산  : 50,000원"
        );

        boolean result = sender.send(context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예산_80퍼센트_경고_이메일_발송")
    void send_warning80_sendEmail() {
        NotificationContext context = new NotificationContext(
                1L,
                recipientEmail,
                null,
                NotificationType.BUDGET_WARNING,
                "[2026-04] 식비 예산 80% 사용",
                "식비 카테고리 예산 현황\n" +
                "예산 한도  : 100,000원\n" +
                "현재 사용  : 80,000원 (80.00%)\n" +
                "남은 예산  : 20,000원"
        );

        boolean result = sender.send(context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예산_100퍼센트_초과_이메일_발송")
    void send_exceeded100_sendEmail() {
        NotificationContext context = new NotificationContext(
                1L,
                recipientEmail,
                null,
                NotificationType.BUDGET_EXCEEDED,
                "[2026-04] 식비 예산 초과!",
                "식비 카테고리 예산 현황\n" +
                "예산 한도  : 100,000원\n" +
                "현재 사용  : 120,000원 (120.00%)\n" +
                "초과 금액  : 20,000원"
        );

        boolean result = sender.send(context);

        assertThat(result).isTrue();
    }

    // ── 헬퍼 메서드 ─────────────────────────────────────────────

    private JavaMailSenderImpl buildMailSender(String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties mailProps = mailSender.getJavaMailProperties();
        mailProps.put("mail.transport.protocol", "smtp");
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    private TemplateEngine buildTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private Properties loadEmailProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("email-test.properties")) {
            if (is != null) {
                props.load(is);
            }
        }
        return props;
    }
}
