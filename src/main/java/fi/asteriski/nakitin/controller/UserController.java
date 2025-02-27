/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.SignupForm;
import fi.asteriski.nakitin.service.UserService;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class UserController {
    private final UserService userService;

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
            Model model) {
        model.addAttribute(MODEL_LABEL_SIGNUP_FORM, signupForm);
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);
        if (result.hasErrors()) {
            addCustomErrorIfNeeded(result, model);
            return "signup";
        }
        userService.createNewUser(signupForm);

        return "redirect:/success";
    }

    @GetMapping("/success")
    public String success(Model model) {
        model.addAttribute(MODEL_LABEL_USER_IS_LOGGED_IN, false);

        return "success";
    }

    private void addCustomErrorIfNeeded(BindingResult result, Model model) {
        if (isPasswordError(result)) {
            model.addAttribute(MODEL_LABEL_CUSTOM_VALIDATION_ERROR, true);
            model.addAttribute(MODEL_LABEL_CUSTOM_ERROR_MESSAGE, "Salasanat eivät täsmää.");
        }
    }

    private boolean isPasswordError(BindingResult result) {
        return result.getAllErrors().stream()
                .anyMatch(a -> Objects.equals(PASSWORDS_MUST_MATCH, a.getDefaultMessage()));
    }
}
