/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.utils.Utils.trimStackTrace;

import fi.asteriski.nakitin.dao.ErrorDao;
import fi.asteriski.nakitin.entity.ErrorEntity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class ErrorService {

    private final ErrorDao errorDao;

    @Transactional
    public UUID logError(RuntimeException ex) {
        log.error(ex.getMessage(), ex.getCause() != null ? trimStackTrace(ex.getCause()) : trimStackTrace(ex));
        return errorDao.logError(ex);
    }

    public Page<ErrorEntity> fetchAllErrorsForAdmin(int page) {
        return errorDao.fetchAllErrorsForAdmin(page);
    }

    public ErrorEntity fetchError(UUID id) {
        return errorDao.fetchError(id);
    }
}
