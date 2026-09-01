package com.francis.taratulong.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {
    String attemptedEmail;
    public UserAlreadyExistsException(String message, String attemptedEmail) {
        super(message);
        this.attemptedEmail = attemptedEmail;
    }

}
