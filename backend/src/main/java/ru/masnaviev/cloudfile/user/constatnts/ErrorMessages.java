package ru.masnaviev.cloudfile.user.constatnts;

public class ErrorMessages {
    //Security & Sessions
    public static final String BAD_CREDENTIALS = "Bad credentials";
    public static final String ACCESS_DENIED = "Access Denied";
    public static final String UNAUTHORIZED = "Unauthorized";

    //Validation
    public static final String UNDEFINED_VALIDATION_ERROR = "";
    public static final String USERNAME_MUST_NOT_BE_EMPTY = "Username must not be empty";
    public static final String USERNAME_LENGTH_BETWEEN_8_50 = "Username length must be between 8 and 50";
    public static final String PASSWORD_MUST_NOT_BE_EMPTY = "Password must not be empty";
    public static final String PASSWORD_LENGTH_BETWEEN_8_100 = "Username length must be between 8 and 50";

    //Other
    public static final String USER_ALREADY_EXISTS = "User with this username already exists";
    public static final String USERNAME_NOT_FOUND = "Username not found";
}
