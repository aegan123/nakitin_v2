/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.controller.ControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.SignupForm;
import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final OrganizationService organizationService;
    private final EventTaskService eventTaskService;
    private final EmailService emailService;
    private final MessageSource messageSource;
    private final RateLimitService rateLimitService;

    @GetMapping("/signup")
    public String signUp(Model model) {
        model.addAttribute(MODEL_LABEL_SIGNUP_FORM, new SignupForm());
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);
        model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, false);

        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(
            @Validated @ModelAttribute(MODEL_LABEL_SIGNUP_FORM) SignupForm signupForm,
            BindingResult result,
            Model model,
            HttpServletRequest request) {
        model.addAttribute(MODEL_LABEL_SIGNUP_FORM, signupForm);
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);
        if (result.hasErrors()) {
            addCustomErrorIfNeeded(result, model);
            return "signup";
        }
        userService.createNewUser(signupForm, request);

        return "redirect:/success?from=signup";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserEntity loggedInUser, Boolean success) {
        setCommonUserAttributes(model, loggedInUser);
        var user = userService.fetchUserDetails(loggedInUser.getId());
        model.addAttribute(
                MODEL_LABEL_USERS_ORGANIZATIONS, organizationService.fetchUsersOrganizations(loggedInUser.getId()));
        model.addAttribute(MODEL_LABEL_USERS_TASKS, eventTaskService.fetchUsersEventTasks(loggedInUser));
        model.addAttribute(MODEL_LABEL_USER, user);
        model.addAttribute(MODEL_LABEL_USER_DTO, user);
        model.addAttribute(MODEL_LABEL_SUCCESS, success);
        model.addAttribute(MODEL_LABEL_FAILED, false);

        return "profile";
    }

    @PostMapping("/profile")
    public String profile(
            @Valid @ModelAttribute(MODEL_LABEL_USER_DTO) UserDto userDto,
            BindingResult result,
            @AuthenticationPrincipal UserEntity user,
            Model model,
            HttpServletRequest request) {
        if (result.hasErrors()) {
            setCommonUserAttributes(model, user);
            model.addAttribute(
                    MODEL_LABEL_USERS_ORGANIZATIONS, organizationService.fetchUsersOrganizations(user.getId()));
            model.addAttribute(MODEL_LABEL_USERS_TASKS, eventTaskService.fetchUsersEventTasks(user));
            model.addAttribute(MODEL_LABEL_USER, userService.fetchUserDetails(user.getId()));
            model.addAttribute(MODEL_LABEL_FAILED, true);
            return "profile";
        }
        userDto.setId(user.getId());
        userService.updateUser(userDto, request);

        return "redirect:/profile?success=true";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model, Locale locale) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);

        if (!rateLimitService.tryConsumeLimitByEmail(email)) {
            model.addAttribute(
                    MODEL_LABEL_ERROR, messageSource.getMessage("auth.error.too.many.reset.attempts", null, locale));
            return "auth/forgot-password";
        }

        var dbUser = userService.findByEmail(email);
        dbUser.ifPresent(user -> {
            var token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);

            var resetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/reset-password")
                    .queryParam("token", token)
                    .build()
                    .toUriString();

            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });

        model.addAttribute(
                MODEL_LABEL_MESSAGE, messageSource.getMessage("auth.message.reset.email.sent", null, locale));
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, Locale locale) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);

        if (!userService.validatePasswordResetToken(token)) {
            model.addAttribute(MODEL_LABEL_ERROR, messageSource.getMessage("auth.error.token.invalid", null, locale));
            return "auth/forgot-password";
        }

        model.addAttribute(MODEL_LABEL_TOKEN, token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handlePasswordReset(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            Locale locale) {

        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);

        if (!password.equals(confirmPassword)) {
            model.addAttribute(
                    MODEL_LABEL_ERROR, messageSource.getMessage("auth.error.passwords.dont.match", null, locale));
            model.addAttribute(MODEL_LABEL_TOKEN, token);
            return "auth/reset-password";
        }

        if (userService.resetPassword(token, password)) {
            return "redirect:/login?reset=success";
        }

        model.addAttribute(
                MODEL_LABEL_ERROR, messageSource.getMessage("auth.error.password.reset.failed", null, locale));
        return "auth/reset-password";
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String reset,
            @RequestParam(required = false) String verificationSent,
            Model model,
            Locale locale) {

        if (error != null) {
            if ("disabled".equals(error)) {
                // Don't set MODEL_LABEL_ERROR here as it would override the template's handling
            } else if ("locked".equals(error)) {
                model.addAttribute(
                        MODEL_LABEL_ERROR, messageSource.getMessage("auth.error.account.locked", null, locale));
            } else {
                model.addAttribute(
                        MODEL_LABEL_ERROR, messageSource.getMessage("auth.error.invalid.credentials", null, locale));
            }
        }
        if (logout != null) {
            model.addAttribute(
                    MODEL_LABEL_MESSAGE, messageSource.getMessage("auth.message.logout.success", null, locale));
        }
        if (reset != null && reset.equals("success")) {
            model.addAttribute(
                    MODEL_LABEL_MESSAGE, messageSource.getMessage("auth.message.password.reset.success", null, locale));
        }

        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);
        return "auth/login";
    }

    private void addCustomErrorIfNeeded(BindingResult result, Model model) {
        if (isPasswordError(result)) {
            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            model.addAttribute(MODEL_LABEL_CUSTOM_ERROR_MESSAGE, PASSWORDS_MUST_MATCH);
        }
    }

    private boolean isPasswordError(BindingResult result) {
        return result.getAllErrors().stream()
                .anyMatch(a -> Objects.equals(PASSWORDS_MUST_MATCH, a.getDefaultMessage()));
    }
}
