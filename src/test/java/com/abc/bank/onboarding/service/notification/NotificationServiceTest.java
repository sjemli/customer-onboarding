package com.abc.bank.onboarding.service.notification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void should_send_success_email_when_fromEmail_is_configured() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "test@gmail.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationService.notifySuccess("seif@domain.com", "Test message");

        verify(mailSender).send(any(MimeMessage.class));
    }


    @Test
    void should_log_warning_for_success_when_FromEmail_is_null() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", null);

        notificationService.notifySuccess("seif@domain.com", "Test message");

        verifyNoInteractions(mailSender);
    }

    @Test
    void should_log_warning_for_success_when_FromEmail_is_blank() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "");

        notificationService.notifySuccess("seif@domain.com", "Test message");

        verifyNoInteractions(mailSender);
    }

    @Test
    void should_send_failure_email_when_FromEmail_is_configured() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "test@gmail.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationService.notifyFailure("seif@domain.com", "Test message");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void should_log_warning_for_failure_when_FromEmail_is_not_configured() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "");

        notificationService.notifyFailure("seif@domain.com", "Test message");

        verifyNoInteractions(mailSender);
    }

    @Test
    void should_log_error_when_send_email_throws_exception() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "test@gmail.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailAuthenticationException("Test exception"))
                .when(mailSender).send(any(MimeMessage.class));

        notificationService.notifySuccess("seif@domain.com", "Test message");

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(logsList.getFirst().getFormattedMessage()).contains("Error occurred while sending email");
    }
}