package ru.masnaviev.cloudstorage.exception.resource;

public class InvalidResourceTypeChangeException extends RuntimeException {
    public InvalidResourceTypeChangeException(String message) {
        super(message);
    }
}
