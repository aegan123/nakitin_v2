/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller.admin;

import static fi.asteriski.nakitin.controller.admin.AdminControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.admin.DeleteForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.admin.AdminEventService;
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
public class AdminEventController {
    private final AdminEventService adminEventService;

    @GetMapping("/admin/events")
    public String adminEvents(
            Model model, @AuthenticationPrincipal UserEntity user, Boolean success, String from, Integer page) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_EVENTS, adminEventService.fetchAllEvents(page == null ? 0 : page - 1));
        model.addAttribute(MODEL_LABEL_SUCCESS, success);
        model.addAttribute(MODEL_LABEL_FROM, from);

        return "admin/events";
    }

    @GetMapping("/admin/view-event")
    public String adminEditEvent(Model model, @AuthenticationPrincipal UserEntity user, UUID id) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_EVENT, adminEventService.fetchEvent(id));

        return "admin/viewEvent";
    }

    @GetMapping("/admin/delete-event-confirmation")
    public String adminDeleteUserConfirmation(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_EVENT, adminEventService.fetchEvent(id));
        model.addAttribute(MODEL_LABEL_ADMIN_DELETE_FORM, new DeleteForm(id));

        return "admin/deleteEventConfirmation";
    }

    @PostMapping("/admin/delete-event")
    public String adminDeleteUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_DELETE_FORM) DeleteForm deleteEventForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        if (result.hasErrors()) {
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_ADMIN_DELETE_FORM, deleteEventForm);
            return "admin/deleteEventConfirmation";
        }
        adminEventService.deleteEvent(deleteEventForm.id());

        return "redirect:/admin/events?success=true&from=delete";
    }
}
