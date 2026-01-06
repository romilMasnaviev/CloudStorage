package ru.masnaviev.cloudfile.exception.resource;

public class FileReadException extends RuntimeException {
    public FileReadException(String message, Throwable ex) {
        super(message, ex);
    }
}
