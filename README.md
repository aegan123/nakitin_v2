# Nakitin
© Juhani Vähä-Mäkilä (juhani@fmail.co.uk) and contributors 2025.

Licenced under EUPL-1.2 or later.

Nakitin is a system for event organizers to add tasks for volunteers to do during events e.g. being a waiter.

Everyone is free to contribute to this project. All contributors must follow the [code of conduct](code_of_conduct.md).

## Features

Currently implemented features:

- Frontpage, single event's page, organization listing page, signup page, login page and privacy policy page are
  accessible to anyone.
    - Except for search engine bots, only the frontpage is allowed.
    - AI crawler bots are blocked as much as possible.
- Organization's admins can add/edit/delete their events and tasks in those events.
- People can sign up.
- Signed-in users can volunteer to tasks or cancel their volunteering.
- Signed-in users can edit their profile and remove their account.
- Customized login flow with email verification and password reset functionalities.
- Email sending.
- All user inputted data is validated.
- Super admin can manage users, organizations, events and view error logs.
- Scheduled tasks to send reminder emails and to clean up the database from expired data.

## Development

See the [development instructions](docs/development_instructions.md).

## Running in production

Prerequisite: Postgres should be already running somewhere and the database exists.

1. Create an .env file somewhere with values for all the variables listed in
   the [environment variables](#environment-variables) table.
2. Run container
    1. `podman run -d --pod=new:eventsignup -p 8080:8080 --env-file=path/to/.env ghcr.io/asteriskiry/nakitin:<tag>` or
    2. `docker run -d -p 8080:8080 --env-file=path/to/.env ghcr.io/asteriskiry/nakitin:<tag>`

Note

- Substitute port numbers with the port you want to run the service with (must be the same as SERVER_PORT env variable).
- Substitute `<tag>` with a version tag e.g. 1.0.0.

## Environment variables

For production these variables are needed.

|                Variable                 |                          Description                           |           Example           | Default |                     Required                      |
|:---------------------------------------:|:--------------------------------------------------------------:|:---------------------------:|:-------:|:-------------------------------------------------:|
|               SERVER_PORT               |             Which port the server is listening in              |            8080             |         |                         Y                         |
|               SERVER_HOST               |                            Hostname                            |          localhost          |         |                         Y                         |
|            SERVER_ENABLE_SSL            |                 Whether to enable SSL support                  |            true             |         |                         Y                         |
|                 DB_HOST                 |                 Hostname of postgresql server                  |          localhost          |         |                         Y                         |
|                 DB_PORT                 |                Which port postgresql is running                |            5432             |         |                         Y                         |
|                 DB_NAME                 |                   Name of the used database                    |        databaseName         |         |                         Y                         |
|               DB_USERNAME               |                   User to connect to db with                   |            user             |         |                         Y                         |
|               DB_PASSWORD               |                    Database user's password                    |          password           |         |                         Y                         |
|                SMTP_HOST                |                    Hostname of SMTP server                     |          localhost          |         |                         Y                         |
|                SMTP_PORT                |                Port what SMTP server listens to                |             25              |         |                         Y                         |
|              SMTP_USERNAME              |                       SMTP server's user                       |            user             |         |                         Y                         |
|              SMTP_PASSWORD              |                      SMTP user's password                      |          password           |         |                         Y                         |
|            PASSWORD_MAX_AGE             |            How long users password is valid in days            |             365             |         |                         Y                         |
| DAYS_BEFORE_SENDING_EXPIRY_NOTIFICATION |          Days before user's expiry to send reminders           |             30              |   30    |                         N                         |
|  MONTHS_BEFORE_DELETING_EXPIRED_USERS   |     How many moths after a user has expired to delete them     |             12              |   12    |                         N                         |
|     PASSWORD_RESET_TOKEN_EXPIRATION     |          How many hours password reset token is valid          |             24              |   24    |                         N                         |
|      RATE_LIMIT_MAX_LOGIN_ATTEMPTS      |      After how many login attempts user gets rate limited      |              5              |    5    |                         N                         |
|      RATE_LIMIT_MAX_REFILL_AMOUNT       |         How long a user is blocked after rate limiting         |              5              |    5    |                         N                         |
|      RATE_LIMIT_MAX_REFILL_MINUTES      |         How long a user is blocked after rate limiting         |             15              |   15    |                         N                         |
| RATE_LIMIT_PASSWORD_RESET_MAX_ATTEMPTS  | After how many password resets attempts user gets rate limited |              3              |    3    |                         N                         |
| RATE_LIMIT_PASSWORD_RESET_REFILL_AMOUNT |         How long a user is blocked after rate limiting         |              3              |    3    |                         N                         |
| RATE_LIMIT_PASSWORD_RESET_REFILL_HOURS  |         How long a user is blocked after rate limiting         |              1              |    1    |                         N                         |
|        CACHE_LOGIN_EXPIRY_HOURS         |          How long are login attempts cached in hours           |              1              |    1    |                         N                         |
|    CACHE_PASSWORD_RESET_EXPIRY_HOURS    |      How long are password reset attempts cached in hours      |             12              |   12    |                         N                         |
|      REMEMBER_ME_VALIDITY_SECONDS       |       How long rememberMe (session) is valid in seconds        |             365             |         |                         Y                         |
|             REMEMBER_ME_KEY             |           Securely generated string to use as a key            | somethingMoreSecureThenThis |         |                         Y                         |
|          ENABLE_ADMIN_CREATION          |               Enable initial admin user creation               |            false            |  false  | Only if an initial admin user needs to be created |
|             ADMIN_USERNAME              |                        Admin's username                        |            admin            |         |     Only if an initial admin user is created      |
|             ADMIN_PASSWORD              |                        Admin's password                        | somethingMoreSecureThenThis |         |     Only if an initial admin user is created      |
|               ADMIN_EMAIL               |                         Admin's email                          |      admin@example.com      |         |     Only if an initial admin user is created      |
|            ADMIN_FIRST_NAME             |                       Admin's first name                       |            admin            |         |     Only if an initial admin user is created      |
|             ADMIN_LAST_NAME             |                       Admin's last name                        |            admin            |         |     Only if an initial admin user is created      |

