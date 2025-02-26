/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.EventForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.EventService;
import fi.asteriski.nakitin.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserService userService;

    @GetMapping("/add-event")
    public String addEvent(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonAttributesForAddEditPage(new EventForm(), model, user);
        model.addAttribute(MODEL_LABEL_IS_EDIT, false);

        return "addEditEvent";
    }

    @PostMapping("/add-event")
    public String addEvent(
            @Valid @ModelAttribute(MODEL_LABEL_EVENT_FORM) EventForm eventForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonAttributesForAddEditPage(eventForm, model, user);
        if (result.hasErrors()) {
            return "addEditEvent";
        }
        var eventId = eventService.createNewEvent(eventForm);

        return String.format("redirect:/event/%s", eventId);
    }

    @GetMapping("/edit-event")
    public String editEvent(Model model, @AuthenticationPrincipal UserEntity user, UUID eventId) {
        setCommonAttributesForAddEditPage(eventService.generateEventFormForEvent(eventId), model, user);
        model.addAttribute(MODEL_LABEL_IS_EDIT, true);

        return "addEditEvent";
    }

    @PostMapping("/edit-event")
    public String editEvent(
            @Valid @ModelAttribute(MODEL_LABEL_EVENT_FORM) EventForm eventForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonAttributesForAddEditPage(eventForm, model, user);
        if (result.hasErrors()) {
            return "addEditEvent";
        }
        var eventId = eventService.editEvent(eventForm);

        return String.format("redirect:/event/%s", eventId);
    }

    private void setCommonAttributesForAddEditPage(EventForm eventForm, Model model, UserEntity user) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, user != null);
        model.addAttribute(MODEL_LABEL_USER_IS_ORGANISATION_ADMIN, user != null && user.isOrganisationAdmin());
        model.addAttribute(MODEL_LABEL_EVENT_FORM, eventForm);
        model.addAttribute(MODEL_LABEL_USER, userService.fetchUserById(user.getId()));
    }
}
