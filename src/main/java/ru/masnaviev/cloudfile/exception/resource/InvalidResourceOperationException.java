package ru.masnaviev.cloudfile.exception.resource;

public class InvalidResourceOperationException extends RuntimeException {
    public InvalidResourceOperationException(String message) {
        super(message);
    }
}
