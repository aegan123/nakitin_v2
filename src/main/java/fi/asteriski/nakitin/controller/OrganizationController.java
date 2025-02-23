/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.NakitinService;
import fi.asteriski.nakitin.service.OrganizationService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final NakitinService nakitinService;

    @GetMapping("/organizations")
    public String organizations(UUID id, Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, loggedInUser != null);
        model.addAttribute("organizations", organizationService.fetchAllOrganization());
        model.addAttribute(MODEL_LABEL_UPCOMING_EVENTS, List.of());
        model.addAttribute(MODEL_LABEL_PAST_EVENTS, List.of());
        model.addAttribute("isOrgInfo", id != null);
        if (id != null) {
            model.addAttribute(MODEL_LABEL_UPCOMING_EVENTS, nakitinService.fetchUpcomingEvents(id));
            model.addAttribute(MODEL_LABEL_PAST_EVENTS, nakitinService.fetchPastEvents(id));
        }

        return "organizations";
    }
}
