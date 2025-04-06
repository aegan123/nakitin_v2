/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.controller.ControllerHelper.setCommonUserAttributes;

import fi.asteriski.nakitin.entity.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrivacyController {
    @GetMapping("/privacy")
    public String privacy(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);

        return "privacy";
    }
}
