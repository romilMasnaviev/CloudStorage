package ru.masnaviev.cloudfile.exception.resource;

public class InvalidResourceTypeChangeException extends RuntimeException {
    public InvalidResourceTypeChangeException(String message) {
        super(message);
    }
}
