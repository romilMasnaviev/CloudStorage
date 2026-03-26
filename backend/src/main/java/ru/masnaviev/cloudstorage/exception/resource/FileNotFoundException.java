package ru.masnaviev.cloudstorage.exception.resource;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String message) {
        super(message);
    }

}
