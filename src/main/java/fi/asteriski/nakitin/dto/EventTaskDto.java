/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventTaskDto(
        String taskName, LocalDate date, LocalTime startTime, LocalTime endTime, Integer personCount) {}
