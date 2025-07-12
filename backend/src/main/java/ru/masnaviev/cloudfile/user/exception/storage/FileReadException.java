package ru.masnaviev.cloudfile.user.exception.storage;

public class FileReadException extends RuntimeException {
    public FileReadException(String message, Throwable ex) {
        super(message, ex);
    }
}
