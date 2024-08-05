package ru.bank.omniproductcatalog.model.exception;

public record ValidationError(String field, String message) {
}
