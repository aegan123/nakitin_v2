/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller.admin;

import static fi.asteriski.nakitin.controller.ControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.DeleteForm;
import fi.asteriski.nakitin.dto.PasswordForm;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.admin.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    @GetMapping({"/admin", "/admin/users"})
    public String adminFrontpage(
            Model model, @AuthenticationPrincipal UserEntity user, Boolean success, String from, Integer page) {
        setCommonUserAttributes(model, user);
        setCommonTabConfigs(model);
        model.addAttribute(MODEL_LABEL_USERS, adminUserService.fetchAllUsersForAdmin(page == null ? 0 : page - 1));
        model.addAttribute(MODEL_LABEL_SUCCESS, success);
        model.addAttribute(MODEL_LABEL_FROM, from);

        return "admin/users";
    }

    @GetMapping("/admin/edit-user")
    public String adminEditUser(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        setCommonTabConfigs(model);
        model.addAttribute(MODEL_LABEL_ADMIN_USER_INFO, adminUserService.fetchUserForm(id));
        model.addAttribute(MODEL_LABEL_ADMIN_PASSWORD_FORM, adminUserService.fetchPasswordForm(id));
        model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS, adminUserService.fetchAllOrganizations());

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
            setCommonTabConfigs(model);
            model.addAttribute(MODEL_LABEL_ADMIN_USER_INFO, userInfoForm);
            model.addAttribute(
                    MODEL_LABEL_ADMIN_PASSWORD_FORM, adminUserService.fetchPasswordForm(userInfoForm.getId()));
            model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS, adminUserService.fetchAllOrganizations());
            return "admin/editUser";
        }

        adminUserService.editUser(userInfoForm);

        return "redirect:/admin/users?success=true&from=edit";
    }

    @GetMapping("/admin/add-user")
    public String adminAddUser(Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        setCommonTabConfigs(model);
        model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_FORM, new AddUserForm());
        model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS, adminUserService.fetchAllOrganizations());

        return "admin/addUser";
    }

    @PostMapping("/admin/add-user")
    public String adminAddUser(
            @Valid @ModelAttribute(MODEL_LABEL_ADMIN_ADD_USER_FORM) AddUserForm addUserForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user,
            HttpServletRequest request) {
        if (result.hasErrors()) {
            setCommonUserAttributes(model, user);
            setCommonTabConfigs(model);
            model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_FORM, addUserForm);
            model.addAttribute(MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS, adminUserService.fetchAllOrganizations());
            addCustomErrorIfNeeded(result, model);
            return "admin/addUser";
        }
        adminUserService.createNewUser(addUserForm, request);

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
            setCommonTabConfigs(model);
            model.addAttribute(MODEL_LABEL_ADMIN_PASSWORD_FORM, passwordForm);
            return "admin/editUser";
        }
        adminUserService.changePasswordForUser(passwordForm);

        return "redirect:/admin/users?success=true&from=pw";
    }

    @GetMapping("/admin/delete-user-confirmation")
    public String adminDeleteUserConfirmation(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        setCommonTabConfigs(model);
        model.addAttribute(MODEL_LABEL_USER, adminUserService.fetchUser(id));
        model.addAttribute(MODEL_LABEL_USER_IS_THE_ONLY_ADMIN, adminUserService.userIsTheOnlyAdmin(user.getId()));
        model.addAttribute(MODEL_LABEL_DELETE_FORM, DeleteForm.builder().id(id).build());

        return "admin/deleteUserConfirmation";
    }

    @PostMapping("/admin/delete-user")
    public String adminDeleteUser(
            @Valid @ModelAttribute(MODEL_LABEL_DELETE_FORM) DeleteForm deleteUserForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        if (result.hasErrors()) {
            setCommonTabConfigs(model);
            model.addAttribute(MODEL_LABEL_DELETE_FORM, deleteUserForm);
            return "admin/deleteUserConfirmation";
        }
        adminUserService.deleteUser(deleteUserForm.id());

        return "redirect:/admin/users?success=true&from=delete";
    }

    @GetMapping("/admin/enable-user")
    public String adminEnableUser(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        setCommonTabConfigs(model);

        adminUserService.enableUser(id);

        return "redirect:/admin/users?success=true&from=enable-user";
    }

    @GetMapping("/admin/disable-user")
    public String adminDisableUser(UUID id, Model model, @AuthenticationPrincipal UserEntity user) {
        setCommonUserAttributes(model, user);
        setCommonTabConfigs(model);
        if (user.isAdmin() && adminUserService.userIsTheOnlyAdmin(id)) {
            return "redirect:/admin/users?success=false&from=disable-user";
        }

        adminUserService.disableUser(id);

        return "redirect:/admin/users?success=true&from=disable-user";
    }

    private void addCustomErrorIfNeeded(BindingResult result, Model model) {
        var errorMsg = "";
        if (isPasswordError(result)) {
            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            errorMsg +=
                    messageSource.getMessage("validation.passwords.mustMatch", null, LocaleContextHolder.getLocale());
        }
        if (isAdminAndOrgAdminError(result)) {
            var message = messageSource.getMessage(
                    "validation.user.notOrgAdminAndAdmin", null, LocaleContextHolder.getLocale());

            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            errorMsg += "<br/>" + message;
        }
        model.addAttribute(MODEL_LABEL_CUSTOM_ERROR_MESSAGE, errorMsg);
    }

    private boolean isPasswordError(BindingResult result) {
        var message = messageSource.getMessage("validation.passwords.mustMatch", null, LocaleContextHolder.getLocale());
        return result.getAllErrors().stream().anyMatch(a -> Objects.equals(message, a.getDefaultMessage()));
    }

    private boolean isAdminAndOrgAdminError(BindingResult result) {
        var message =
                messageSource.getMessage("validation.user.notOrgAdminAndAdmin", null, LocaleContextHolder.getLocale());
        return result.getAllErrors().stream().anyMatch(a -> Objects.equals(message, a.getDefaultMessage()));
    }

    private static void setCommonTabConfigs(Model model) {
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, true);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ERRORS_TAB, false);
    }
}
