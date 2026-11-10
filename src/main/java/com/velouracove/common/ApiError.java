package com.velouracove.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(OffsetDateTime timestamp, int status, String code, String message, List<FieldError> fieldErrors) {
    public record FieldError(String field, String message) {}
}
