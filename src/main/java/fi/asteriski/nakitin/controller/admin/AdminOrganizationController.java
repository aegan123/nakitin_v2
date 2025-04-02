/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller.admin;

import static fi.asteriski.nakitin.controller.admin.AdminControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.admin.AddEditOrganizationForm;
import fi.asteriski.nakitin.dto.admin.DeleteForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.admin.AdminOrganizationService;
import jakarta.validation.Valid;
import java.util.Objects;
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
public class AdminOrganizationController {
    private final AdminOrganizationService adminOrganizationService;

    @GetMapping("/admin/organizations")
    public String adminOrganizations(
            Model model, @AuthenticationPrincipal UserEntity user, Boolean success, String from) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_ORGANIZATIONS, adminOrganizationService.fetchAllOrganizations());
        model.addAttribute(MODEL_LABEL_SUCCESS, success);
        model.addAttribute(MODEL_LABEL_FROM, from);

        return "admin/organizations";
    }

    @GetMapping("/admin/add-organization")
    public String adminAddUser(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(
                MODEL_LABEL_ADMIN_ADD_EDIT_ORGANIZATION_FORM, adminOrganizationService.fetchOrganizationAddForm());
        model.addAttribute(MODEL_LABEL_IS_EDIT, false);

        return "admin/addEditOrganization";
    }

    @PostMapping("/admin/add-organization")
    public String adminAddUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_ADD_EDIT_ORGANIZATION_FORM)
                    AddEditOrganizationForm addOrganizationForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        if (result.hasErrors()) {
            setCommonUserAttributes(model, user);
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_ADMIN_ADD_EDIT_ORGANIZATION_FORM, addOrganizationForm);
            model.addAttribute(MODEL_LABEL_IS_EDIT, false);
            return "admin/addEditOrganization";
        }
        adminOrganizationService.createNewOrganization(addOrganizationForm);

        return "redirect:/admin/organizations?success=true&from=add";
    }

    @GetMapping("/admin/edit-organization")
    public String adminEditUser(Model model, @AuthenticationPrincipal UserEntity user, UUID id) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(
                MODEL_LABEL_ADMIN_ADD_EDIT_ORGANIZATION_FORM, adminOrganizationService.fetchOrganizationEditForm(id));
        model.addAttribute(MODEL_LABEL_IS_EDIT, true);

        return "admin/addEditOrganization";
    }

    @PostMapping("/admin/edit-organization")
    public String adminEditUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_ADD_EDIT_ORGANIZATION_FORM)
                    AddEditOrganizationForm editOrganizationForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        if (isOrganizationNameValidationError(result) && result.hasErrors()) {
            adminOrganizationService.addUsersToAddEditOrganizationForm(editOrganizationForm);
            setCommonUserAttributes(model, user);
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_EDIT, true);
            addCustomErrorIfNeeded(result, model);
            return "admin/addEditOrganization";
        }

        adminOrganizationService.editOrganization(editOrganizationForm);

        return "redirect:/admin/organizations?success=true&from=edit";
    }

    @GetMapping("/admin/delete-organization-confirmation")
    public String adminDeleteUserConfirmation(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_ORGANIZATION, adminOrganizationService.fetchOrganization(id));
        model.addAttribute(MODEL_LABEL_ADMIN_DELETE_FORM, new DeleteForm(id));

        return "admin/deleteOrganizationConfirmation";
    }

    @PostMapping("/admin/delete-organization")
    public String adminDeleteUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_DELETE_FORM) DeleteForm deleteOrganizationForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        if (result.hasErrors()) {
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_ADMIN_DELETE_FORM, deleteOrganizationForm);
            return "admin/deleteOrganizationConfirmation";
        }
        adminOrganizationService.deleteOrganization(deleteOrganizationForm.id());

        return "redirect:/admin/organizations?success=true&from=delete";
    }

    private void addCustomErrorIfNeeded(BindingResult result, Model model) {
        var errorMsg = "";
        if (isOrganizationNameValidationError(result)) {
            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            errorMsg += ORGANIZATION_ALREADY_EXISTS_BY_THIS_NAME;
        }
        model.addAttribute(MODEL_LABEL_CUSTOM_ERROR_MESSAGE, errorMsg);
    }

    private boolean isOrganizationNameValidationError(BindingResult result) {
        return result.getAllErrors().stream()
                .anyMatch(f -> Objects.equals(f.getDefaultMessage(), ORGANIZATION_ALREADY_EXISTS_BY_THIS_NAME));
    }
}
