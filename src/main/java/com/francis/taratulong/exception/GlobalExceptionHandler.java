package com.francis.taratulong.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserExist(UserAlreadyExistsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "CONFLICT");
        response.put("message", "Email already used.");
        response.put("duplicateEmail", ex.getAttemptedEmail());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

}
