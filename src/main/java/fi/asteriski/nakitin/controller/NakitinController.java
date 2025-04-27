/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.controller.ControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.service.NakitinService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class NakitinController {
    private final NakitinService nakitinService;

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        setCommonUserAttributes(model, loggedInUser);
        model.addAttribute(MODEL_LABEL_UPCOMING_EVENTS, nakitinService.fetchUpcomingEvents());

        return "index";
    }

    @GetMapping("/event/{eventId}")
    public String eventPage(
            @PathVariable UUID eventId,
            Model model,
            @AuthenticationPrincipal UserEntity loggedInUser,
            Boolean success) {
        setCommonUserAttributes(model, loggedInUser);
        model.addAttribute(MODEL_LABEL_EVENT, nakitinService.fetchEventForEventPage(eventId));
        model.addAttribute(MODEL_LABEL_USER, loggedInUser);
        model.addAttribute(MODEL_LABEL_SUCCESS, success);

        return "eventPage";
    }

    @GetMapping("/task/add")
    public String addEventTaskForm(UUID eventId, Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        setCommonUserAttributes(model, loggedInUser);
        model.addAttribute(MODEL_LABEL_EVENT_TASK_FORM, new EventTaskForm());
        model.addAttribute(MODEL_LABEL_EVENT, nakitinService.fetchEvent(eventId));
        model.addAttribute(MODEL_LABEL_IS_EDIT, false);

        return "addEditEventTaskPage";
    }

    @PostMapping("/task/add")
    public String addEventTask(
            @Valid @ModelAttribute(MODEL_LABEL_EVENT_TASK_FORM) EventTaskForm eventTaskForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity loggedInUser) {
        setCommonUserAttributes(model, loggedInUser);
        model.addAttribute(MODEL_LABEL_EVENT_TASK_FORM, eventTaskForm);
        model.addAttribute(MODEL_LABEL_EVENT, nakitinService.fetchEvent(eventTaskForm.getEventId()));
        model.addAttribute(MODEL_LABEL_IS_EDIT, false);

        if (result.hasErrors()) {
            return "addEditEventTaskPage";
        }
        nakitinService.addEventTask(eventTaskForm);

        return "redirect:/event/%s".formatted(eventTaskForm.getEventId());
    }

    @GetMapping("/task/edit")
    public String editEventTaskForm(
            UUID eventId, UUID taskId, Model model, @AuthenticationPrincipal UserEntity loggedInUser) {
        var event = nakitinService.fetchEvent(eventId);
        var task = event.tasks().stream()
                .filter(t -> t.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new EventTaskNotFoundException("No task found with id " + taskId));
        model.addAttribute(
                MODEL_LABEL_EVENT_TASK_FORM,
                EventTaskForm.builder()
                        .id(task.id())
                        .taskName(task.taskName())
                        .date(task.date())
                        .startTime(task.startTime())
                        .endTime(task.endTime())
                        .personCount(task.personCount())
                        .eventId(eventId)
                        .build());
        model.addAttribute(MODEL_LABEL_EVENT, event);
        model.addAttribute(MODEL_LABEL_IS_EDIT, true);
        setCommonUserAttributes(model, loggedInUser);

        return "addEditEventTaskPage";
    }

    @PostMapping("/task/edit")
    public String editEventTask(
            @Valid @ModelAttribute(MODEL_LABEL_EVENT_TASK_FORM) EventTaskForm eventTaskForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity loggedInUser) {
        setCommonUserAttributes(model, loggedInUser);
        model.addAttribute(MODEL_LABEL_EVENT_TASK_FORM, eventTaskForm);
        model.addAttribute(MODEL_LABEL_EVENT, nakitinService.fetchEvent(eventTaskForm.getEventId()));
        model.addAttribute(MODEL_LABEL_IS_EDIT, true);
        if (result.hasErrors()) {
            return "addEditEventTaskPage";
        }
        nakitinService.editEventTask(eventTaskForm);

        return "redirect:/event/%s".formatted(eventTaskForm.getEventId());
    }

    @PostMapping("/volunteer/{eventId}/task/{taskId}")
    public String volunteerToTask(
            @PathVariable UUID eventId,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserEntity loggedInUser,
            Boolean cancel) {
        if (Boolean.TRUE.equals(cancel)) {
            nakitinService.cancelVolunteeringToTask(taskId, loggedInUser);
        } else {
            nakitinService.volunteerToTask(taskId, loggedInUser);
        }

        return "redirect:/event/%s".formatted(eventId);
    }

    @GetMapping("/success")
    public String success(Model model, @AuthenticationPrincipal UserEntity loggedInUser, String from) {
        setCommonUserAttributes(model, loggedInUser);
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);
        model.addAttribute(MODEL_LABEL_FROM, from);

        return "success";
    }
}
