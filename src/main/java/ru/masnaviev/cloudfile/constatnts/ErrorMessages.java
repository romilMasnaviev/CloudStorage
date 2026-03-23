package ru.masnaviev.cloudfile.constatnts;

public class ErrorMessages {
    //Security & Sessions
    public static final String BAD_CREDENTIALS = "Bad credentials";
    public static final String ACCESS_DENIED = "Access Denied";
    public static final String UNAUTHORIZED = "Unauthorized";

    //Validation
    public static final String USERNAME_MUST_NOT_BE_EMPTY = "Username must not be empty";
    public static final String USERNAME_LENGTH_BETWEEN_8_50 = "Username length must be between 8 and 50";
    public static final String PASSWORD_MUST_NOT_BE_EMPTY = "Password must not be empty";
    public static final String PASSWORD_LENGTH_BETWEEN_8_100 = "Username length must be between 8 and 50";
    public static final String PATH_MUST_NOT_BE_EMPTY = "Path must not be empty";
    public static final String USERID_MUST_NOT_BE_LESS_0 = "User id must not be less 0";

    //Other
    public static final String USER_ALREADY_EXISTS = "User with this username already exists";
    public static final String USERNAME_NOT_FOUND = "Username not found";

    //Minio
    public static final String MINIO_EXCEPTION = "Minio exception";
    public static final String FILE_ALREADY_EXIST = "File already exists";
    public static final String DIRECTORY_ALREADY_EXISTS = "Directory already exists";
    public static final String PROTECTED_PARENT_DIRECTORY = "Parent directory is protected and cannot be removed";

    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String PATH_NOT_FOUND = "Path not found";
    public static final String DIRECTORY_NOT_FOUND = "Directory not found";
    public static final String FILE_NOT_FOUND = "File not found";
    public static final String INVALID_OPERATION_COMBINATION = "Invalid operation combination";
    public static final String INVALID_RESOURCE_TYPE_CHANGE = "Invalid resource type change";


    public static final String PATH_MUST_BE_END_SLASH = "Path must end with the character '/'";
}
