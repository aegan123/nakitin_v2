/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetUrl);

    void sendEmailVerification(String toEmail, String verificationUrl);

    void sendPasswordExpirationWarning(String toEmail, int daysUntilExpiration);
}
