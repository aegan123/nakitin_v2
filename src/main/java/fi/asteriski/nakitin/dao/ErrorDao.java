/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.utils.Constants.*;
import static fi.asteriski.nakitin.utils.Utils.trimStackTrace;

import fi.asteriski.nakitin.entity.ErrorEntity;
import fi.asteriski.nakitin.exceptions.NotFoundException;
import fi.asteriski.nakitin.repo.ErrorRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ErrorDao {

    private ErrorRepository errorRepository;

    public UUID logError(RuntimeException ex) {

        var entity = ErrorEntity.builder()
                .stackTrace(trimStackTrace(ex))
                .errorClass(ex.getClass().getName())
                .errorMessage(ex.getMessage())
                .build();
        return errorRepository.save(entity).getId();
    }

    public Page<ErrorEntity> fetchAllErrorsForAdmin(int page) {
        return errorRepository.findAll(PageRequest.of(page, MAX_PAGE_SIZE, SORT_BY_TIMESTAMP_DESC));
    }

    public ErrorEntity fetchError(UUID id) {
        return errorRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Error not found with id '%s'.".formatted(id)));
    }
}
