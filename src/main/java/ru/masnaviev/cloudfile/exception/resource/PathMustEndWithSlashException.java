package ru.masnaviev.cloudfile.exception.resource;

public class PathMustEndWithSlashException extends RuntimeException {
    public PathMustEndWithSlashException(String message) {
        super(message);
    }
}
