package ru.bank.omniproductcatalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.model.common.ApiError;
import ru.bank.omniproductcatalog.model.exception.BadRequestException;
import ru.bank.omniproductcatalog.model.exception.FieldValidationException;
import ru.bank.omniproductcatalog.model.exception.NotFoundException;
import ru.bank.omniproductcatalog.model.exception.ServiceTimeoutException;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ApiError> handleNotFoundException(final NotFoundException ex, final ServerWebExchange exchange) {
        return Mono.just(new ApiError(
                Instant.now(),
                exchange.getRequest().getPath().toString(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                UUID.randomUUID().toString()
        ));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<ApiError> handleServiceTimeoutException(final ServiceTimeoutException ex, final ServerWebExchange exchange) {
        return Mono.just(new ApiError(
                Instant.now(),
                exchange.getRequest().getPath().toString(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                UUID.randomUUID().toString()
        ));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ApiError> handleFieldValidationException(final FieldValidationException ex, final ServerWebExchange exchange) {
        return Mono.just(new ApiError(
                Instant.now(),
                exchange.getRequest().getPath().toString(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrors().toString(),
                UUID.randomUUID().toString()
        ));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ApiError> handleBadRequestException(final BadRequestException ex, final ServerWebExchange exchange) {
        return Mono.just(new ApiError(
                Instant.now(),
                exchange.getRequest().getPath().toString(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                UUID.randomUUID().toString()
        ));
    }
}
