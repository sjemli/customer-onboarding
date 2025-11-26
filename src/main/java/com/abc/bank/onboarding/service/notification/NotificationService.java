package com.abc.bank.onboarding.service.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Async
    public void notifySuccess(String toEmail, String accountNumber) {
        String body = """
                Your account has been created successfully!
                Account Number: %s
                """.formatted(accountNumber);
        sendEmail(toEmail, "ABC Bank Onboarding Successful", body);
    }

    @Async
    public void notifyFailure(String toEmail, String failureMessage) {
        String body = "Onboarding failed: " + failureMessage;
        sendEmail(toEmail, "ABC Bank Onboarding Failure", body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("Email sender not configured. Falling back to logging: To={}, Subject={}, Body={}",
                    toEmail, subject, body);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            log.error("Error occurred while sending email to {}", toEmail, ex);
        }
    }
}