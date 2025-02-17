/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.service.NakitinService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class NakitinRestController {

    private final NakitinService nakitinService;

    @PostMapping("/event/{eventId}/task/add")
    public void addEventTask(@PathVariable UUID eventId, @Valid @RequestBody EventTaskForm eventTaskForm) {
        nakitinService.addEventTask(eventId, eventTaskForm);
    }

    @PutMapping("/event/task/edit")
    public void editEventTask(@Valid @RequestBody EventTaskForm eventTaskForm) {
        nakitinService.editEventTask(eventTaskForm);
    }

    @GetMapping(value = "/event/{eventId}/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportEvent(@PathVariable UUID eventId) {
        var exportDto = nakitinService.exportVolunteers(eventId);

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.csv", exportDto.eventName()));
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");


        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(exportDto.csv().length())
            .contentType(MediaType.TEXT_PLAIN)
            .body(exportDto.csv());
    }
}
