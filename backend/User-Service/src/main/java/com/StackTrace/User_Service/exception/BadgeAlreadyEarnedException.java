package com.StackTrace.User_Service.exception;

public class BadgeAlreadyEarnedException
        extends BusinessException {

    public BadgeAlreadyEarnedException(
            String message
    ) {
        super(message);
    }
}
