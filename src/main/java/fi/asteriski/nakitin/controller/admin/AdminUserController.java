/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller.admin;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.DeleteUserForm;
import fi.asteriski.nakitin.dto.admin.PasswordForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.AdminUserService;
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
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping({"/admin", "/admin/users"})
    public String adminFrontpage(Model model, @AuthenticationPrincipal UserEntity user, Boolean success, String from) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute("users", adminUserService.fetchAllUsers());
        model.addAttribute("success", success);
        model.addAttribute("from", from);

        return "admin/users";
    }

    @GetMapping("/admin/organizations")
    public String adminOrganizations(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);

        return "admin/organizations";
    }

    @GetMapping("/admin/events")
    public String adminEvents(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, true);

        return "admin/events";
    }

    @GetMapping("/admin/edit-user")
    public String adminEditUser(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_ADMIN_USER_INFO, adminUserService.fetchUserForm(id));
        model.addAttribute(MODEL_LABEL_ADMIN_PASSWORD_FORM, adminUserService.fetchPasswordForm(id));

        return "admin/editUser";
    }

    @PostMapping("/admin/edit-user")
    public String adminEditUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_USER_INFO) UserInfoForm userInfoForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        if (result.hasErrors()) {
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_ADMIN_USER_INFO, userInfoForm);
            model.addAttribute(
                    MODEL_LABEL_ADMIN_PASSWORD_FORM, adminUserService.fetchPasswordForm(userInfoForm.getId()));
            return "admin/editUser";
        }

        adminUserService.editUser(userInfoForm);

        return "redirect:/admin/users?success=true&from=edit";
    }

    @GetMapping("/admin/add-user")
    public String adminAddUser(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_FORM, new AddUserForm());
        model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS, adminUserService.fetchAllOrganizations());

        return "admin/addUser";
    }

    @PostMapping("/admin/add-user")
    public String adminAddUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_ADD_USER_FORM) AddUserForm addUserForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        if (result.hasErrors()) {
            setCommonUserAttributes(model, user);
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_FORM, addUserForm);
            model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS, adminUserService.fetchAllOrganizations());
            addCustomErrorIfNeeded(result, model);
            return "admin/addUser";
        }
        adminUserService.createNewUser(addUserForm);

        return "redirect:/admin/users?success=true&from=add";
    }

    @PostMapping("admin/change-password")
    public String adminChangePassword(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_PASSWORD_FORM) PasswordForm passwordForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        if (result.hasErrors()) {
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_ADMIN_PASSWORD_FORM, passwordForm);
            return "admin/editUser";
        }
        adminUserService.changePasswordForUser(passwordForm);

        return "redirect:/admin/users?success=true&from=pw";
    }

    @GetMapping("/admin/delete-user-confirmation")
    public String adminDeleteUserConfirmation(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_USER, adminUserService.fetchUser(id));
        model.addAttribute("userIsTheOnlyAdmin", adminUserService.userIsTheOnlyAdmin(user.getId()));
        model.addAttribute(MODEL_LABEL_ADMIN_DELETE_USER_FORM, new DeleteUserForm(id));

        return "admin/deleteUserConfirmation";
    }

    @PostMapping("/admin/delete-user")
    public String adminDeleteUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_DELETE_USER_FORM) DeleteUserForm deleteUserForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        if (result.hasErrors()) {
            model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
            model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
            model.addAttribute(MODEL_LABEL_ADMIN_DELETE_USER_FORM, deleteUserForm);
            return "admin/deleteUserConfirmation";
        }
        adminUserService.deleteUser(deleteUserForm.userId());

        return "redirect:/admin/users?success=true&from=delete";
    }

    private void setCommonUserAttributes(Model model, UserEntity user) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, user != null);
        model.addAttribute(MODEL_LABEL_USER_IS_ADMIN, user != null && user.isAdmin());
    }

    private void addCustomErrorIfNeeded(BindingResult result, Model model) {
        var errorMsg = "";
        if (isPasswordError(result)) {
            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            errorMsg += "Salasanat eivät täsmää.";
        }
        if (isAdminAndOrgAdminError(result)) {
            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            errorMsg += "<br/>Käyttäjä ei voi olla sekä järjestön että Nakittimen admin.";
        }
        model.addAttribute(MODEL_LABEL_CUSTOM_ERROR_MESSAGE, errorMsg);
    }

    private boolean isPasswordError(BindingResult result) {
        return result.getAllErrors().stream()
                .anyMatch(a -> Objects.equals(PASSWORDS_MUST_MATCH, a.getDefaultMessage()));
    }

    private boolean isAdminAndOrgAdminError(BindingResult result) {
        return result.getAllErrors().stream()
                .anyMatch(a -> Objects.equals(CANNOT_BE_ADMIN_AND_ORG_ADMIN, a.getDefaultMessage()));
    }
}
