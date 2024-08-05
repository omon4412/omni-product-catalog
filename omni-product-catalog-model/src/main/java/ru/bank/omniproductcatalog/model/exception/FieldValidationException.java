package ru.bank.omniproductcatalog.model.exception;

import java.util.List;

public class FieldValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    public FieldValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
