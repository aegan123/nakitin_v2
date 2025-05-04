/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.test.builders;

import static fi.asteriski.nakitin.test.factory.TestDataFactory.generateRandomString;

import fi.asteriski.nakitin.entity.ErrorEntity;
import java.time.ZonedDateTime;

public class TestErrorBuilder {
    private String errorClass = "java.lang.RuntimeException";
    private String errorMessage = generateRandomString(20);
    private String stackTrace = generateRandomString(100);
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public static TestErrorBuilder builder() {
        return new TestErrorBuilder();
    }

    public TestErrorBuilder withErrorClass(String errorClass) {
        this.errorClass = errorClass;
        return this;
    }

    public TestErrorBuilder withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public TestErrorBuilder withStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public TestErrorBuilder withTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TestErrorBuilder withDefaults() {
        return this;
    }

    public ErrorEntity build() {
        return ErrorEntity.builder()
                .errorClass(errorClass)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .timestamp(timestamp)
                .build();
    }
}
