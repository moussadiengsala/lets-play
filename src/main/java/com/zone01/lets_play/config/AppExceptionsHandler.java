package com.zone01.lets_play.config;

import com.zone01.lets_play.utils.Response;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AppExceptionsHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppExceptionsHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex){
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        errorDetail.setProperty("access_denied_reason", "Authentication Failed!");
        return errorDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex){
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        errorDetail.setProperty("access_denied_reason", "Unauthorized!");
        return errorDetail;
    }

    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwtException(JwtException ex) {
//        logger.error("JWT processing failed: {}", ex.getMessage());
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        errorDetail.setProperty("error_code", "INVALID_TOKEN");
        return errorDetail;
    }

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

    // Handle generic ConstraintViolationException (for @Valid or @Validated annotations)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
//        logger.warn("Constraint violation: {}", ex.getMessage());
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error occurred");
        errorDetail.setProperty("error_code", "VALIDATION_ERROR");
        return errorDetail;
    }

    // Handle AuthenticationException (generic exception for auth issues)
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
//        logger.error("Authentication error: {}", ex.getMessage());
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication failed");
        errorDetail.setProperty("error_code", "AUTH_ERROR");
        return errorDetail;
    }

    // Handle generic exception fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex) {
//        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        errorDetail.setProperty("error_code", "INTERNAL_ERROR");
        return errorDetail;
    }
}


//eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzQGdtYWlsLmNvbSIsImlhdCI6MTcyODkxMDY1NCwiZXhwIjoxNzI4OTk3MDU0fQ.khZAMl3lVfMniFJiJLr8OvlE4Pe5wVJlCW9toF-p2KU6zQMUOzUsRp4jDBBh5961
//eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzMUBnbWFpbC5jb20iLCJpYXQiOjE3Mjg5MTE2NjAsImV4cCI6MTcyODk5ODA2MH0.2ey_YkEH5yLuuFKp7CBa-OdI8E611QEFx-K4nljyQ2Qj8CgZ0xzu4BNO_fFfIGZ6