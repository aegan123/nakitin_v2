/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class Constants {
    public static final Sort SORT_BY_DATE_ASC = Sort.by(Sort.Direction.ASC, "date");
    public static final Sort SORT_BY_LASTNAME_ASC = Sort.by(Sort.Direction.ASC, "lastName");
    public static final Sort SORT_BY_ID_ASC = Sort.by(Sort.Direction.ASC, "id");
    public static final Sort SORT_BY_NAME_ASC = Sort.by(Sort.Direction.ASC, "name");
    public static final int MAX_PAGE_SIZE = 20;
    public static final String DUMMY_PASSWORD = Base64.getEncoder().encodeToString("dummyPassword".getBytes());
    public static final String PASSWORDS_MUST_MATCH = "Salasanojen tulee täsmätä";
    public static final String CANNOT_BE_ADMIN_AND_ORG_ADMIN =
            "Käyttäjä ei voi olla sekä nakittimen admin että järjestöadmin";
    public static final String EMAIL_IN_USE_BY_ANOTHER_USER = "Sähköpostiosoite on jo käytössä toisella käyttäjällä";
    public static final String ORGANIZATION_ALREADY_EXISTS_BY_THIS_NAME = "Tämän niminen järjestö on jo olemassa.";
    public static final String ERROR_MESSAGE_PASSWORDS_DO_NOT_MATCH = "Salasanat eivät täsmää.";
    public static final DateTimeFormatter DATE_FORMAT_FINLAND = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
    public static final String MODEL_LABEL_DELETE_FORM = "deleteForm";
    public static final String MODEL_LABEL_ADMIN_ADD_USER_FORM = "adminAddUserForm";
    public static final String MODEL_LABEL_ADMIN_ADD_USER_ORGANIZATIONS = "adminAddUserOrganizations";
    public static final String MODEL_LABEL_ADMIN_ADD_EDIT_ORGANIZATION_FORM = "adminAddEditOrganization";
    public static final String MODEL_LABEL_SUCCESS = "success";
    public static final String MODEL_LABEL_FROM = "from";
    public static final String MODEL_LABEL_ORGANIZATION = "organization";
    public static final String MODEL_LABEL_USER_IS_THE_ONLY_ADMIN = "userIsTheOnlyAdmin";
    public static final String MODEL_LABEL_USERS = "data";
    public static final String MODEL_LABEL_EVENTS = "data";
    public static final String MODEL_LABEL_ORGANIZATIONS = "data";
    public static final String MODEL_LABEL_USER_ORGANIZATIONS = "userOrganizations";
    public static final String MODEL_LABEL_USERS_ORGANIZATIONS = "usersOrganizations";
    public static final String MODEL_LABEL_USERS_TASKS = "usersTasks";
    public static final String MODEL_LABEL_FAILED = "failed";
    public static final String MODEL_LABEL_ERROR = "error";
    public static final String MODEL_LABEL_MESSAGE = "message";
    public static final String MODEL_LABEL_TOKEN = "token";
}
