/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import fi.asteriski.nakitin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class VerificationController {
    private final UserService userService;

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        var result = userService.verifyEmail(token);

        if (result.verified()) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Sähköpostiosoite vahvistettu onnistuneesti! Voit nyt kirjautua sisään.");
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "Vahvistuslinkki on vanhentunut. Ole hyvä ja pyydä uusi.");
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
