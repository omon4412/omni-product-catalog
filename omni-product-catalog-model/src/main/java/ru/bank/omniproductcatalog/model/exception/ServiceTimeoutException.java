package ru.bank.omniproductcatalog.model.exception;

public class ServiceTimeoutException extends RuntimeException {

    public ServiceTimeoutException(String message) {
        super(message);
    }
}
