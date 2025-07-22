package ru.masnaviev.cloudfile.user.exception.resource;

public class PathMustEndWithSlashException extends RuntimeException {
    public PathMustEndWithSlashException(String message) {
        super(message);
    }
}
