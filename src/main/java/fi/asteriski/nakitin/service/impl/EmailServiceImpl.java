/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.impl;

import fi.asteriski.nakitin.service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText(
                """
            Hello,

            A password reset has been requested for your account.
            To reset your password, please click the link below:

            %s

            If you did not request this password reset, please ignore this email.
            The link will expire in 24 hours.

            Best regards,
            Nakitin Team
            """
                        .formatted(resetUrl));

        mailSender.send(message);
    }

    @Override
    public void sendEmailVerification(String toEmail, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify Your Email Address");
        message.setText(
                """
            Hello,

            Thank you for signing up. Please verify your email address by clicking the link below:

            %s

            This link will expire in 24 hours.

            Best regards,
            Nakitin Team
            """
                        .formatted(verificationUrl));

        mailSender.send(message);
    }

    @Override
    public void sendPasswordExpirationWarning(String toEmail, int daysUntilExpiration) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Expiration Warning");
        message.setText(
                """
            Hello,

            Your password will expire in %d days. Please log in to your account and update your password.

            Best regards,
            Nakitin Team
            """
                        .formatted(daysUntilExpiration));

        mailSender.send(message);
    }
}
