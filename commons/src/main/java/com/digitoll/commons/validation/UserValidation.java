package com.digitoll.commons.validation;

public class UserValidation {

    public static final String USERNAME_VALIDATION_REGEXP = "^(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._@]+(?<![_.])$";
    public static final String PASSWORD_VALIDATION_REGEXP = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).+$";

    public static final int USERNAME_SIZE_MIN = 4;
    public static final int USERNAME_SIZE_MAX = 100; // the username can be an email

    public static final int PASSWORD_SIZE_MIN = 8;
    public static final int PASSWORD_SIZE_MAX = 250;

    public static final String USERNAME_VALIDATION_MESSAGE = "can only contain alphanumeric characters, " +
            "underscores and dots. Must not begin or end with a dot or an underscore.";

    public static final String PASSWORD_VALIDATION_MESSAGE = "must contain one digit, one lower and " +
            "one upper alphabetic character.";

    public static final String USERNAME_SIZE_MESSAGE = "must be at least 4 and maximum 100 characters long.";
    public static final String PASSWORD_SIZE_MESSAGE = "must be at least 8 characters long.";

}
