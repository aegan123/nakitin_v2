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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final RateLimitService rateLimitService;
    private final MessageSource messageSource;

    public CustomAuthenticationFailureHandler(RateLimitService rateLimitService, MessageSource messageSource) {
        this.rateLimitService = rateLimitService;
        this.messageSource = messageSource;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        String ip = request.getRemoteAddr();
        boolean canTry = rateLimitService.tryConsumeLimitByIp(ip);

        String errorMessage = messageSource.getMessage(
                canTry ? "auth.error.invalid.credentials" : "auth.error.too.many.attempts", null, request.getLocale());

        String redirectUrl = "/login?error=true&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        super.setDefaultFailureUrl(redirectUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
