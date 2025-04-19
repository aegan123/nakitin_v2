/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.config;

import fi.asteriski.nakitin.service.RateLimitService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final RateLimitService rateLimitService;
    private final MessageSource messageSource;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        var ip = request.getRemoteAddr();
        boolean canTry = rateLimitService.tryConsumeLimitByIp(ip);

        String redirectUrl;
        if (!canTry) {
            var errorMessage = messageSource.getMessage("auth.error.too.many.attempts", null, request.getLocale());
            redirectUrl = "/login?error=true&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        } else if (exception instanceof DisabledException) {
            redirectUrl = "/login?error=disabled";
        } else {
            var errorMessage = messageSource.getMessage("auth.error.invalid.credentials", null, request.getLocale());
            redirectUrl = "/login?error=true&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        }

        super.setDefaultFailureUrl(redirectUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
