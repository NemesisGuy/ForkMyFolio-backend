package com.forkmyfolio.model.enums;

/**
 * Defines the academic level of a qualification.
 */
public enum QualificationLevel {
    CERTIFICATE,
    DIPLOMA,
    ASSOCIATE,
    BACHELORS,
    HONOURS,
    MASTERS,
    DOCTORATE;

    public boolean isBlank() {
        return this == null || this.name().isBlank();

    }
}