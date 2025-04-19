/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.UserDao;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@AllArgsConstructor
public class ScheduledTasksService {

    private final UserDao userDao;

    @Scheduled(cron = "${fi.asteriski.config.cleanup.unverified-users-cron}")
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.info("Starting cleanup of unverified users with expired tokens.");
        var toDelete = userDao.findUnverifiedUsersWithExpiredTokens();
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} unverified users with expired tokens.", toDelete.size());
            userDao.deleteUsersById(toDelete);
        }
    }
}
