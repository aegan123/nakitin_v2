/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class Constants {
    public static final String MODEL_LABEL_EVENT_TASK_FORM = "eventTaskForm";
    public static final String MODEL_LABEL_USER_IS_LOGGED_IN = "userIsLoggedIn";
    public static final String MODEL_LABEL_EVENT = "event";
    public static final String MODEL_LABEL_IS_EDIT = "isEdit";
    public static final String MODEL_LABEL_USER_IS_ORGANISATION_ADMIN = "userIsOrganisationAdmin";
    public static final Sort SORT_BY_DATE_ASC = Sort.by(Sort.Direction.ASC, "date");
    public static final String MODEL_LABEL_UPCOMING_EVENTS = "upcomingEvents";
    public static final String MODEL_LABEL_PAST_EVENTS = "pastEvents";
}
