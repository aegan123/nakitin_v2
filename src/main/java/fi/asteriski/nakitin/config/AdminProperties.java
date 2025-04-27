/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later..
 */
package fi.asteriski.nakitin.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.admin")
@Profile("!test")
@ConditionalOnProperty(name = "app.admin.initialization.enabled", havingValue = "true")
@Data
public class AdminProperties {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
