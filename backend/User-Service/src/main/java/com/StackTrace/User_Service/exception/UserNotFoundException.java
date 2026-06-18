package com.StackTrace.User_Service.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
