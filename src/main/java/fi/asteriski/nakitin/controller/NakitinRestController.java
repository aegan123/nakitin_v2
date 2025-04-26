/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.MEDIA_TYPE_TEXT_CSV_CHARSET_UTF_8;
import static org.springframework.http.HttpHeaders.*;

import fi.asteriski.nakitin.service.NakitinService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class NakitinRestController {

    private final NakitinService nakitinService;

    @GetMapping(value = "/export", produces = MEDIA_TYPE_TEXT_CSV_CHARSET_UTF_8)
    public ResponseEntity<String> exportEvent(UUID eventId) {
        var exportDto = nakitinService.exportVolunteers(eventId);

        var headers = new HttpHeaders();
        headers.add(
                CONTENT_DISPOSITION,
                String.format(
                        "attachment; filename*=UTF-8''%s.csv",
                        URLEncoder.encode(exportDto.eventName().replace(" ", "_"), StandardCharsets.UTF_8)));
        headers.add(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(PRAGMA, "no-cache");
        headers.add(EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(exportDto.csv().getBytes(StandardCharsets.UTF_8).length)
                .contentType(MediaType.parseMediaType(MEDIA_TYPE_TEXT_CSV_CHARSET_UTF_8))
                .body(exportDto.csv());
    }
}
