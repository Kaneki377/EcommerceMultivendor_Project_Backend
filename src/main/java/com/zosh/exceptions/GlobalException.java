package com.zosh.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalException {


    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorDetails> ProductExceptionHandler(ProductException pe, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setError(pe.getMessage());
        errorDetails.setDetails(request.getDescription(false));
        errorDetails.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    //bắt error bằng @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errorMessage.append(fieldError.getField())
                        .append(" - ")
                        .append(fieldError.getDefaultMessage())
                        .append("; ")
        );

        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setTimestamp(LocalDateTime.now());
        errorDetails.setError(errorMessage.toString());
        errorDetails.setDetails(request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

}
