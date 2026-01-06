package ru.masnaviev.cloudfile.exception.resource;

public class DirectoryAlreadyExistsException extends RuntimeException {
    public DirectoryAlreadyExistsException(String message) {
        super(message);
    }
}
