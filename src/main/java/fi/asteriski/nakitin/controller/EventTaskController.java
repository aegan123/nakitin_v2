/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.controller.ControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.MODEL_LABEL_DELETE_FORM;

import fi.asteriski.nakitin.dto.DeleteForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.EventTaskService;
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
public class EventTaskController {
    private final EventTaskService eventTaskService;

    @GetMapping("/task/delete")
    public String deleteTask(Model model, UUID eventId, UUID taskId, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_DELETE_FORM, new DeleteForm(taskId, eventId));
        model.addAttribute("task", eventTaskService.readEventTaskById(taskId));

        return "deleteTaskConfirmation";
    }

    @PostMapping("/task/delete")
    public String deleteTask(
            @Valid @ModelAttribute(MODEL_LABEL_DELETE_FORM) DeleteForm deleteForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        if (result.hasErrors()) {
            setCommonUserAttributes(model, user);
            model.addAttribute(MODEL_LABEL_DELETE_FORM, deleteForm);
            model.addAttribute("task", eventTaskService.readEventTaskById(deleteForm.id()));
            return "deleteTaskConfirmation";
        }
        eventTaskService.deleteTask(deleteForm.id());

        return String.format("redirect:/event/%s?success=true", deleteForm.id2());
    }
}
