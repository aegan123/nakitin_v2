/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.NakitinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class NakitinController {
    private final NakitinService nakitinService;

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        model.addAttribute("userIsLoggedIn", loggedInUser != null);
        model.addAttribute("upcomingEvents", nakitinService.fetchUpcomingEvents());
        return "index";
    }
}
