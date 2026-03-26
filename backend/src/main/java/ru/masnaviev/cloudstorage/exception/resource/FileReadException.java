package ru.masnaviev.cloudstorage.exception.resource;

public class FileReadException extends RuntimeException {
    public FileReadException(String message, Throwable ex) {
        super(message, ex);
    }
}
