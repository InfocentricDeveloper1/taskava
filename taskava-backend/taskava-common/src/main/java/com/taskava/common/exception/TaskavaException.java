package com.taskava.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TaskavaException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object[] args;

    public TaskavaException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = null;
    }

    public TaskavaException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = null;
    }

    public TaskavaException(String message, String errorCode, HttpStatus httpStatus, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = args;
    }

    // Specific exception types
    public static class NotFoundException extends TaskavaException {
        public NotFoundException(String message) {
            super(message, "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        public NotFoundException(String entityName, Object id) {
            super(String.format("%s not found with id: %s", entityName, id), 
                  "NOT_FOUND", HttpStatus.NOT_FOUND);
        }
    }

    public static class BadRequestException extends TaskavaException {
        public BadRequestException(String message) {
            super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }
    }

    public static class UnauthorizedException extends TaskavaException {
        public UnauthorizedException(String message) {
            super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
    }

    public static class ForbiddenException extends TaskavaException {
        public ForbiddenException(String message) {
            super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
    }

    public static class ConflictException extends TaskavaException {
        public ConflictException(String message) {
            super(message, "CONFLICT", HttpStatus.CONFLICT);
        }
    }

    public static class ValidationException extends TaskavaException {
        public ValidationException(String message) {
            super(message, "VALIDATION_ERROR", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public static class InternalServerException extends TaskavaException {
        public InternalServerException(String message) {
            super(message, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        public InternalServerException(String message, Throwable cause) {
            super(message, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause);
        }
    }
}