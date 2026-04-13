package com.francis.taratulong.exception;

public class UserAlreadyExistsException extends RuntimeException {
    String attemptedEmail;
    public UserAlreadyExistsException(String message, String attemptedEmail) {
        super(message);
        this.attemptedEmail = attemptedEmail;
    }

    public String getAttemptedEmail() {
        return attemptedEmail;
    }
}
