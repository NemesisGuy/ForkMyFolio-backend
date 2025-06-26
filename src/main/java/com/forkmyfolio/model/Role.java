package com.forkmyfolio.model;

/**
 * Defines the roles a user can have within the application.
 * Currently supports USER and ADMIN roles.
 */
public enum Role {
    /**
     * Standard user role with basic permissions.
     */
    USER,

    /**
     * Administrator role with elevated permissions,
     * typically for managing content and users.
     */
    ADMIN
}
