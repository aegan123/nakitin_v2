/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RobotsTxtController {
    @GetMapping(
            value = {"/robots.txt", "/robot.txt", "/Robots.txt", "/Robot.txt"},
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String getRobotsTxt() {
        return """
                User-agent: *
                Disallow: /
                """;
    }
}
