package fi.asteriski.nakitin.dto;

import java.time.ZonedDateTime;

public record EventDto(String name, String venue, String description, ZonedDateTime date) {}
