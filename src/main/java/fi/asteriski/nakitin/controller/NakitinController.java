/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.NakitinService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
@AllArgsConstructor
public class NakitinController {
    private final NakitinService nakitinService;

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        model.addAttribute("userIsLoggedIn", loggedInUser != null);
        model.addAttribute("upcomingEvents", nakitinService.fetchUpcomingEvents());
        return "index";
    }

    @GetMapping("/event/{eventId}")
    public String eventPage(@PathVariable UUID eventId, Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        //        model.addAttribute("userIsLoggedIn", loggedInUser != null);
        model.addAttribute("userIsLoggedIn", true);
        model.addAttribute("userHasVolunteered", loggedInUser != null && loggedInUser.hasVolunteered(eventId));
        //        model.addAttribute("userIsOrganisationAdmin", loggedInUser != null &&
        // loggedInUser.isOrganisationAdmin());
        model.addAttribute("userIsOrganisationAdmin", true);
        model.addAttribute("event", nakitinService.fetchEvent(eventId));
        return "eventPage";
    }

    @PostMapping("/event/{eventId}/task/{taskId}/volunteer")
    public String volunteerToTask(@PathVariable UUID eventId, @PathVariable UUID taskId, @AuthenticationPrincipal UserEntity loggedInUser) {
        nakitinService.volunteerToTask(taskId, loggedInUser);

        return "redirect:/event/" + eventId;
    }

    @PostMapping("/event/{eventId}/task/{taskId}/cancel")
    public String cancelVolunteeringToTask(@PathVariable UUID eventId, @PathVariable UUID taskId, @AuthenticationPrincipal UserEntity loggedInUser) {
        nakitinService.cancelVolunteeringToTask(taskId, loggedInUser);

        return "redirect:/event/" + eventId;
    }
}
