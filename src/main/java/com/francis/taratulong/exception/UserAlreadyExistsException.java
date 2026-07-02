package com.francis.taratulong.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends BaseNotFoundException {
    String attemptedEmail;
    public UserAlreadyExistsException(String message, String attemptedEmail) {
        super(message);
        this.attemptedEmail = attemptedEmail;
    }

}
