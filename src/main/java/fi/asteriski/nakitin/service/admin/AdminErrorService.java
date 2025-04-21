/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import static fi.asteriski.nakitin.utils.Utils.convertWhitespaceToHtml;

import fi.asteriski.nakitin.entity.ErrorEntity;
import fi.asteriski.nakitin.service.ErrorService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminErrorService {
    private final ErrorService errorService;

    public Page<ErrorEntity> fetchAllErrors(int page) {
        return errorService.fetchAllErrorsForAdmin(page);
    }

    public ErrorEntity fetchError(UUID id) {
        var entity = errorService.fetchError(id);
        return ErrorEntity.builder()
                .id(entity.getId())
                .stackTrace(convertWhitespaceToHtml(entity.getStackTrace()))
                .errorClass(entity.getErrorClass())
                .errorMessage(entity.getErrorMessage())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
