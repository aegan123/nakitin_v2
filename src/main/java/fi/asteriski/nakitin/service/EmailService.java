/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.utils.Constants.LOG_ERROR_MESSAGE_TEMPLATE;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    @NonNull
    private final JavaMailSender mailSender;

    @NonNull
    private final MessageSource messageSource;

    @Value("${fi.asteriski.config.email.default-sender-address}")
    private String defaultSender;

    @Value("${fi.asteriski.config.passwordResetTokenExpirationHours}")
    private Integer tokenExpirationHours;

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        var subject = messageSource.getMessage("email.subject.password-reset", null, LocaleContextHolder.getLocale());
        var message = messageSource.getMessage(
                "email.message.password-reset",
                new Object[] {resetUrl, tokenExpirationHours},
                LocaleContextHolder.getLocale());
        try {
            sendEmail(toEmail, defaultSender, subject, message);
        } catch (MessagingException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(e));
        }
    }

    public void sendEmailVerification(String toEmail, String verificationUrl) {
        var subject = messageSource.getMessage("email.subject.verification", null, LocaleContextHolder.getLocale());
        var message = messageSource.getMessage(
                "email.message.verification",
                new Object[] {verificationUrl, tokenExpirationHours},
                LocaleContextHolder.getLocale());
        try {
            sendEmail(toEmail, defaultSender, subject, message);
        } catch (MessagingException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(e));
        }
    }

    public void sendPasswordExpirationWarning(final String[] recipients, final Long daysUntilExpiration) {
        var subject =
                messageSource.getMessage("email.subject.password-expiration", null, LocaleContextHolder.getLocale());
        var message = messageSource.getMessage(
                "email.message.password-expiration",
                new Object[] {daysUntilExpiration},
                LocaleContextHolder.getLocale());
        try {
            sendEmail(recipients, defaultSender, subject, message);
        } catch (MessagingException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(e));
        }
    }

    public void sendEmailChangeVerification(String toEmail, String verificationUrl) {
        var subject = messageSource.getMessage("email.subject.verification", null, LocaleContextHolder.getLocale());
        var message = messageSource.getMessage(
                "email.message.email-change-verification",
                new Object[] {verificationUrl, tokenExpirationHours},
                LocaleContextHolder.getLocale());
        try {
            sendEmail(toEmail, defaultSender, subject, message);
        } catch (MessagingException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(e));
        }
    }

    public void sendAccountDeletionWarning(final String[] recipients, final Long monthsToDeleteExpiredUsers) {
        var subject = messageSource.getMessage("email.subject.account-deletion", null, LocaleContextHolder.getLocale());
        var message = messageSource.getMessage(
                "email.message.email-account-deletion",
                new Object[] {monthsToDeleteExpiredUsers},
                LocaleContextHolder.getLocale());
        try {
            sendEmail(recipients, defaultSender, subject, message);
        } catch (MessagingException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(e));
        }
    }

    public void sendEmailVerificationForAdmin(String email, String userName, String verificationUrl) {
        var subject = messageSource.getMessage("email.subject.verification", null, LocaleContextHolder.getLocale());
        var message = messageSource.getMessage(
                "email.message.verificationAdmin",
                new Object[] {userName, verificationUrl, tokenExpirationHours},
                LocaleContextHolder.getLocale());
        try {
            sendEmail(email, defaultSender, subject, message);
        } catch (MessagingException e) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(e));
        }
    }

    private void sendEmail(String recipient, String sender, String messageSubject, String messageText)
            throws MessagingException {
        var msg = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(msg);
        helper.setTo(recipient);
        helper.setFrom(sender);
        helper.setSubject(messageSubject);
        helper.setText(messageText, true);

        sendMessage(msg);
    }

    private void sendEmail(
            final String[] recipients, final String sender, final String messageSubject, final String messageText)
            throws MessagingException {
        var msg = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(msg);
        helper.setBcc(recipients);
        helper.setFrom(sender);
        helper.setSubject(messageSubject);
        helper.setText(messageText, true);

        sendMessage(msg);
    }

    private void sendMessage(final MimeMessage msg) {
        try {
            mailSender.send(msg);
        } catch (MailAuthenticationException mailAuthenticationException) {
            log.error("Error authenticating to smtp server.", mailAuthenticationException);
        } catch (MailSendException mailSendException) {
            log.error("Error sending email.", mailSendException);
        } catch (MailException mailException) {
            log.error(LOG_ERROR_MESSAGE_TEMPLATE.formatted(mailException));
        }
    }
}
