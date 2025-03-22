/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import java.util.Base64;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class Constants {
    public static final Sort SORT_BY_DATE_ASC = Sort.by(Sort.Direction.ASC, "date");
    public static final Sort SORT_BY_LASTNAME_ASC = Sort.by(Sort.Direction.ASC, "lastName");
    public static final String DUMMY_PASSWORD = Base64.getEncoder().encodeToString("dummyPassword".getBytes());
    public static final String PASSWORDS_MUST_MATCH = "Salasanojen tulee täsmätä";
    public static final String CANNOT_BE_ADMIN_AND_ORG_ADMIN =
            "Käyttäjä ei voi olla sekä nakittimen admin että organisaatioadmin";
    public static final String EMAIL_IN_USE_BY_ANOTHER_USER = "Sähköpostiosoite on jo käytössä toisella käyttäjällä";
    public static final String MODEL_LABEL_EVENT_TASK_FORM = "eventTaskForm";
    public static final String MODEL_LABEL_USER_IS_LOGGED_IN = "userIsLoggedIn";
    public static final String MODEL_LABEL_EVENT = "event";
    public static final String MODEL_LABEL_IS_EDIT = "isEdit";
    public static final String MODEL_LABEL_USER_IS_ORGANISATION_ADMIN = "userIsOrganisationAdmin";
    public static final String MODEL_LABEL_UPCOMING_EVENTS = "upcomingEvents";
    public static final String MODEL_LABEL_PAST_EVENTS = "pastEvents";
    public static final String MODEL_LABEL_EVENT_FORM = "eventForm";
    public static final String MODEL_LABEL_USER = "user";
    public static final String MODEL_LABEL_SIGNUP_FORM = "signupForm";
    public static final String MODEL_LABEL_CUSTOM_VALIDATION_ERROR = "customValidationError";
    public static final String MODEL_LABEL_CUSTOM_ERROR_MESSAGE = "customValidationErrorMessage";
    public static final String MODEL_LABEL_USER_IS_ADMIN = "userIsAdmin";
    public static final String MODEL_LABEL_IS_USER_MANAGEMENT_TAB = "isUserManagementTab";
    public static final String MODEL_LABEL_IS_ORGANIZATION_MANAGEMENT_TAB = "isOrganizationManagementTab";
    public static final String MODEL_LABEL_IS_EVENT_MANAGEMENT_TAB = "isEventManagementTab";
    public static final String MODEL_LABEL_USER_DTO = "userDto";
    public static final String MODEL_LABEL_ADMIN_USER_INFO = "adminUserInfo";
    public static final String MODEL_LABEL_ADMIN_PASSWORD_FORM = "adminPasswordForm";
    public static final String MODEL_LABEL_ADMIN_DELETE_USER_FORM = "adminDeleteUserForm";
    public static final String MODEL_LABEL_ADMIN_ADD_USER_FORM = "adminAddUserForm";
    public static final String MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS = "adminAddUserOrganizations";
}
