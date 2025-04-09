/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo.projection;

import java.time.LocalDate;

public interface NameAndDateProjection {
    String getName();

    LocalDate getDate();
}
