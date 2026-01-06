package ru.masnaviev.cloudfile.exception.resource;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String message) {
        super(message);
    }
}
