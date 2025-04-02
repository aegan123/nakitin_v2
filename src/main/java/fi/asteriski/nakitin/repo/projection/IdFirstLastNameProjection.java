/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo.projection;

import java.util.UUID;

public interface IdFirstLastNameProjection {
    UUID getId();

    String getFirstName();

    String getLastName();
}
