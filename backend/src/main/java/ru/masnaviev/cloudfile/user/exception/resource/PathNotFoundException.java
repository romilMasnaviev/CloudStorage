package ru.masnaviev.cloudfile.user.exception.resource;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String message) {
        super(message);
    }
}
