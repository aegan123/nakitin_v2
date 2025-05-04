/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

public enum UserRole {
    ROLE_ADMIN("ADMIN"),
    ROLE_USER("USER"),
    ROLE_ORG_ADMIN("ORG_ADMIN");

    public final String label;

    UserRole(String label) {
        this.label = label;
    }
}
