package ru.masnaviev.cloudfile.user.exception.resource;

public class FileAlreadyExistsException extends RuntimeException {
    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
