/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.PasswordResetTokenDao;
import fi.asteriski.nakitin.dao.UserDao;
import fi.asteriski.nakitin.dao.VerificationTokenDao;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.VerificationTokenEntity;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@AllArgsConstructor
public class ScheduledTasksService {

    private final VerificationTokenDao verificationTokenDao;
    private final UserDao userDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final UserService userService;

    @Scheduled(cron = "${fi.asteriski.config.cleanup.deleteUnverifiedUsersCron}")
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.info("Starting cleanup of unverified users with expired tokens.");
        var toDelete = verificationTokenDao.findUnverifiedUsersWithExpiredTokens();
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} unverified users with expired tokens.", toDelete.size());
            var users = toDelete.stream().map(VerificationTokenEntity::getUser).toList();
            toDelete.forEach(token -> token.removeUser(token.getUser()));
            verificationTokenDao.deleteByUsers(users);
            userDao.deleteUsersById(users.stream()
                    .filter(UserEntity::isNotAdmin)
                    .map(UserEntity::getId)
                    .toList());
        }
    }

    @Scheduled(cron = "${fi.asteriski.config.cleanup.passwordResetTokensCron}")
    @Transactional
    public void cleanupPasswordResetTokens() {
        log.info("Starting cleanup of expired password reset tokens.");
        var toDelete = passwordResetTokenDao.findExpiredTokens();
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} expired tokens.", toDelete.size());
            passwordResetTokenDao.deleteAll(toDelete);
        }
    }

    @Scheduled(cron = "${fi.asteriski.config.cleanup.expiringPasswordReminderCron}")
    @Transactional
    public void sendReminderAboutExpiringPassword() {
        log.info("Starting sending reminders about expiring passwords.");
        userService.sendReminderEmailAboutExpiringPasswords();
    }

    @Scheduled(cron = "${fi.asteriski.config.cleanup.disableExpiringUsersCron}")
    @Transactional
    public void disableExpiredUsers() {
        log.info("Starting disabling expired users.");
        userService.disableExpiredUsers();
    }

    @Scheduled(cron = "${fi.asteriski.config.cleanup.deleteExpiredUsersCron}")
    @Transactional
    public void deleteExpiredUsers() {
        log.info("Starting delete expired users.");
        userService.deleteExpiredUsers();
    }

    @Scheduled(cron = "${fi.asteriski.config.cleanup.accountDeletionReminderCron}")
    @Transactional
    public void sendReminderToUsersAboutAccountDeletion() {
        log.info("Starting send reminder to users about account deletion.");
        userService.sendReminderToExpiringUser();
    }
}
