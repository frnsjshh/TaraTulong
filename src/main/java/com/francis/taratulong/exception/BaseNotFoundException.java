package com.francis.taratulong.exception;

public abstract class BaseNotFoundException extends RuntimeException{
    public BaseNotFoundException(String message) {
        super(message);
    }
}
