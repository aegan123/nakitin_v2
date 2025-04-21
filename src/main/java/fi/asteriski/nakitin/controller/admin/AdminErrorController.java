/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller.admin;

import static fi.asteriski.nakitin.controller.ControllerHelper.setCommonUserAttributes;
import static fi.asteriski.nakitin.utils.Constants.*;
import static fi.asteriski.nakitin.utils.Utils.extractUrlPath;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.admin.AdminErrorService;
import fi.asteriski.nakitin.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class AdminErrorController {
    private final AdminErrorService adminErrorService;

    @GetMapping("/admin/errors")
    public String adminError(Model model, @AuthenticationPrincipal UserEntity user, Integer page) {
        setCommonUserAttributes(model, user);
        setCommonTabConfig(model);
        model.addAttribute(MODEL_LABEL_ERRORS, adminErrorService.fetchAllErrors(page == null ? 0 : page - 1));

        return "admin/errors";
    }

    @GetMapping("/admin/view-error")
    public String adminViewError(
            Model model, @AuthenticationPrincipal UserEntity user, UUID id, HttpServletRequest request) {
        setCommonUserAttributes(model, user);
        setCommonTabConfig(model);
        model.addAttribute(MODEL_LABEL_ERROR_INFO, adminErrorService.fetchError(id));
        var referer = Optional.ofNullable(request.getHeader(HttpHeaders.REFERER))
                .map(Utils::extractUrlPath)
                .orElse("admin/errors");
        model.addAttribute("path", referer.contains("?") ? referer.substring(0, referer.indexOf("?")) : referer);
        model.addAttribute("page", referer.contains("?") ? referer.charAt(referer.length() - 1) : "1");
        extractUrlPath(referer);

        return "admin/viewError";
    }

    private static void setCommonTabConfig(Model model) {
        model.addAttribute(MODEL_LABEL_IS_USER_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB, false);
        model.addAttribute(MODEL_LABEL_IS_ERRORS_TAB, true);
    }
}
