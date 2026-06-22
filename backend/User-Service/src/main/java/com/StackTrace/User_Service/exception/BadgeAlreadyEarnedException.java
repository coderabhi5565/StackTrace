package com.StackTrace.User_Service.exception;

public class BadgeAlreadyEarnedException
        extends RuntimeException {

    public BadgeAlreadyEarnedException(
            String message
    ) {
        super(message);
    }
}
