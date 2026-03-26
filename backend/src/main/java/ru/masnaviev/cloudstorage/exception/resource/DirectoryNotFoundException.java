package ru.masnaviev.cloudstorage.exception.resource;

public class DirectoryNotFoundException extends RuntimeException {

    public DirectoryNotFoundException(String message) {
        super(message);
    }

}
