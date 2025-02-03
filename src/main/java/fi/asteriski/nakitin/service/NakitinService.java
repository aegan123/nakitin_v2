/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.NakitinDao;
import fi.asteriski.nakitin.dto.EventDto;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class NakitinService {
    private final NakitinDao nakitinDao;

    public List<EventDto> fetchUpcomingEvents() {
        return nakitinDao.fetchUpcomingEvents();
    }
}
