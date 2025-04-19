/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

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

    @Scheduled(cron = "${fi.asteriski.config.cleanup.unverified-users-cron}")
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.info("Starting cleanup of unverified users with expired tokens.");
        var toDelete = verificationTokenDao.findUnverifiedUsersWithExpiredTokens();
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} unverified users with expired tokens.", toDelete.size());
            var users = toDelete.stream().map(VerificationTokenEntity::getUser).toList();
            toDelete.forEach(token -> token.removeUser(token.getUser()));
            verificationTokenDao.deleteByUsers(users);
            userDao.deleteUsersById(users.stream().map(UserEntity::getId).toList());
        }
    }
}
