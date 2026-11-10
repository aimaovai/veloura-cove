package com.velouracove.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception) {
        return response(exception.getStatus(), exception.getCode(), exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<ApiError.FieldError> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(this::fieldError).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "One or more request fields are invalid.", fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraint(ConstraintViolationException exception) {
        List<ApiError.FieldError> fields = exception.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(v.getPropertyPath().toString(), v.getMessage())).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "One or more request fields are invalid.", fields);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> handleMalformedRequest(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request contains an invalid value or format.", List.of());
    }

    @ExceptionHandler({MissingRequestHeaderException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ApiError> handleMissingRequestValue(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, "MISSING_REQUEST_VALUE", "A required request header or parameter is missing.", List.of());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    ResponseEntity<ApiError> handleNotFound(Exception exception) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found.", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethodNotAllowed(Exception exception) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "The request method is not supported for this resource.", List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> handleUnsupportedMediaType(Exception exception) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "The request content type is not supported.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException exception) {
        return response(HttpStatus.CONFLICT, "REQUEST_CONFLICT", "The request conflicts with an existing record.", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "The request could not be completed.", List.of());
    }

    private ApiError.FieldError fieldError(FieldError error) {
        return new ApiError.FieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message, List<ApiError.FieldError> fields) {
        return ResponseEntity.status(status).body(new ApiError(OffsetDateTime.now(), status.value(), code, message, fields));
    }
}
