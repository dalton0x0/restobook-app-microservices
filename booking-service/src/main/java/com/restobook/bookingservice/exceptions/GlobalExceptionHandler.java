package com.restobook.bookingservice.exceptions;

import com.restobook.bookingservice.constants.ExceptionConst;
import com.restobook.bookingservice.dtos.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ErrorResponse> handleBookingException(
            BookingException ex, HttpServletRequest request) {

        log.warn("Booking exception: {} - Code: {} - Path: {}",
                ex.getMessage(), ex.getErrorCode(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        log.warn("Validation error: {} fields invalid - Path: {}", fieldErrors.size(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.withFieldErrors(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erreur de validation des données",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        String message = String.format("Le paramètre '%s' a une valeur invalide: '%s'",
                ex.getName(), ex.getValue());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {

        log.warn("Resource does not exists: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "La ressource demandée n'existe pas",
                ExceptionConst.RESOURCE_NOT_FOUND,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.",
                ExceptionConst.INTERNAL_ERROR,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
