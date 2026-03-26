package ru.masnaviev.cloudstorage.exception.resource;

public class PathMustEndWithSlashException extends RuntimeException {
    public PathMustEndWithSlashException(String message) {
        super(message);
    }
}
