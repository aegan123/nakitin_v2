/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.MODEL_LABEL_MESSAGE;
import static fi.asteriski.nakitin.utils.Constants.MODEL_LABEL_SUCCESS;

import fi.asteriski.nakitin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class VerificationController {
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        var result = userService.verifyEmail(token);
        model.addAttribute("verificationStatus", result.status());

        if (result.verified()) {
            model.addAttribute(MODEL_LABEL_SUCCESS, true);
            model.addAttribute(
                    MODEL_LABEL_MESSAGE,
                    messageSource.getMessage("verification.success.message", null, LocaleContextHolder.getLocale()));
        } else {
            model.addAttribute(MODEL_LABEL_SUCCESS, false);
            model.addAttribute(
                    MODEL_LABEL_MESSAGE,
                    messageSource.getMessage("verification.error.expired", null, LocaleContextHolder.getLocale()));
            model.addAttribute("email", result.email());
        }

        return "auth/verification-result";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email, HttpServletRequest request) {
        userService.findByEmail(email).ifPresent(user -> userService.setupEmailVerification(user, request));
        return "redirect:/login?verificationSent=true";
    }
}
