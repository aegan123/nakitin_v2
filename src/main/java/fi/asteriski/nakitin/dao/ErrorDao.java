/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import com.mgnt.utils.TextUtils;
import fi.asteriski.nakitin.entity.ErrorEntity;
import fi.asteriski.nakitin.repo.ErrorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class ErrorDao {

    private ErrorRepository errorRepository;

    public UUID logError(RuntimeException ex) {

        var entity = ErrorEntity.builder()
                .stackTrace(TextUtils.getStacktrace(ex, true, "fi.asteriski"))
                .errorClass(ex.getClass().getName())
                .errorMessage(ex.getMessage())
                .build();
        return errorRepository.save(entity).getId();
    }
}
