package com.StackTrace.User_Service.exception;

public class BadgeNotFoundException
        extends BusinessException {

    public BadgeNotFoundException(
            String message
    ) {
        super(message);
    }
}

