package ru.masnaviev.cloudstorage.exception.resource;

public class InvalidResourceOperationException extends RuntimeException {
    public InvalidResourceOperationException(String message) {
        super(message);
    }
}
