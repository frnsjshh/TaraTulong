package com.francis.taratulong.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        LocalDateTime timeStamp,
        Integer status,
        String error,
        String message,
        String path

) {
}
