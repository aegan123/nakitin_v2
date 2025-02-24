/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.MODEL_LABEL_USER_IS_LOGGED_IN;
import static fi.asteriski.nakitin.utils.Constants.MODEL_LABEL_USER_IS_ORGANISATION_ADMIN;

import fi.asteriski.nakitin.entity.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrivacyController {
    @GetMapping("/privacy")
    public String privacy(Model model, @AuthenticationPrincipal UserEntity user) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, user != null);
        model.addAttribute(MODEL_LABEL_USER_IS_ORGANISATION_ADMIN, user != null && user.isOrganisationAdmin());

        return "privacy";
    }
}
