package com.zone01.products.config;

import com.zone01.products.utils.Response;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AppExceptionsHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppExceptionsHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Response<Map<String, String>> response = Response.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .data(errors)
                .message("Validation Failed")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // Handle generic exception fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Map<String, String>>> handleGlobalException(Exception ex) {
        HttpStatus status = getStatus(ex);

        String exceptionMessage = ex.getMessage() != null ? ex.getMessage() : "Unknown error occurred";

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error_type", ex.getClass().getSimpleName());
        errorDetails.put("error_message", exceptionMessage);

        Response<Map<String, String>> response = Response.<Map<String, String>>builder()
                .status(status.value())
                .data(errorDetails)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private HttpStatus getStatus(Exception ex) {
        HttpStatus status;
        if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN; // 403 for access denied
        } else if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED; // 401 for authentication failures
        } else {
            status = HttpStatus.BAD_REQUEST; // Default to 500 for general errors
        }
        return status;
    }

}