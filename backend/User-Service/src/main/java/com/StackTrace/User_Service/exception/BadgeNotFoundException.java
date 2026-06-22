package com.StackTrace.User_Service.exception;

public class BadgeNotFoundException
        extends RuntimeException {

    public BadgeNotFoundException(
            String message
    ) {
        super(message);
    }
}

