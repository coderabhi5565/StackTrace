package com.StackTrace.User_Service.exception;

public class UsernameAlreadyExistsException extends BusinessException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
