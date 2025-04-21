/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RobotsTxtController {

    private final String robotsTxt = readText();

    @GetMapping(
            value = {"/robots.txt", "/robot.txt", "/Robots.txt", "/Robot.txt"},
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String getRobotsTxt() {
        return robotsTxt;
    }

    private String readText() {
        try (var inputStream = getClass().getResourceAsStream("/robots.txt")) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new IllegalStateException("robots.txt file not found in resources");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read robots.txt file", e);
        }
    }
}
