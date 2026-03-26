package ru.masnaviev.cloudstorage.exception.resource;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String message) {
        super(message);
    }
}
