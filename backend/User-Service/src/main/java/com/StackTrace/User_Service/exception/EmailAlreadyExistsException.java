package com.StackTrace.User_Service.exception;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
