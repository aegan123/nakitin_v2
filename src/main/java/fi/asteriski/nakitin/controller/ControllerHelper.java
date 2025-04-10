/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.entity.UserEntity;
import lombok.experimental.UtilityClass;
import org.springframework.ui.Model;

@UtilityClass
public class ControllerHelper {
    public static void setCommonUserAttributes(Model model, UserEntity user) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, user != null);
        model.addAttribute(MODEL_LABEL_USER_IS_ADMIN, user != null && user.isAdmin());
        model.addAttribute(MODEL_LABEL_USER_IS_ORGANISATION_ADMIN, user != null && user.isOrganisationAdmin());
    }
}
