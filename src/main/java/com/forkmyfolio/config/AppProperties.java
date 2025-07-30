package com.forkmyfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps application configuration properties from application.properties to a type-safe Java object.
 * This class is the single source of truth for all custom application settings.
 * The `prefix = "app"` means it will only bind properties that start with "app.".
 */
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Oauth2 oauth2 = new Oauth2();
    private final Cookie cookie = new Cookie();
    private final Security security = new Security();

    @Getter
    @Setter
    public static class Jwt {
        /**
         * The Base64-encoded secret key used for signing JWTs.
         * Mapped from `app.jwt.secret`.
         */
        private String secret;

        /**
         * The expiration time for JWT access tokens, in milliseconds.
         * Mapped from `app.jwt.expiration-ms`. Spring Boot automatically handles the conversion
         * from kebab-case in the properties file to camelCase in the Java field.
         */
        private long expirationMs;

        /**
         * The expiration time for refresh tokens, in milliseconds.
         * Mapped from `app.jwt.refresh-expiration-ms`.
         */
        private long refreshExpirationMs;

        /**
         * The name of the cookie used to store the refresh token.
         * Mapped from `app.jwt.refresh-cookie-name`.
         */
        private String refreshCookieName;
    }

    @Getter
    @Setter
    public static final class Oauth2 {
        /**
         * A list of authorized redirect URIs for the OAuth2 client.
         * Mapped from `app.oauth2.authorized-redirect-uris`.
         */
        private List<String> authorizedRedirectUris = new ArrayList<>();
    }

    @Getter
    @Setter
    public static final class Cookie {
        /**
         * Determines if the 'Secure' flag should be set on cookies.
         * Mapped from `app.cookie.secure`.
         */
        private boolean secure;

        /**
         * The SameSite attribute for cookies (e.g., "Lax", "Strict", "None").
         * Mapped from `app.cookie.samesite`.
         */
        private String samesite;
    }

    @Getter
    @Setter
    public static final class Security {
        /**
         * The domain to be set on security-related cookies.
         * Mapped from `app.security.cookie-domain`.
         */
        private String cookieDomain;
    }
}