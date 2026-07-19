package com.stacktrace.comment_service.exception;

public class UnauthorizedCommentAccessException extends RuntimeException {

    public UnauthorizedCommentAccessException(String message) {
        super(message);
    }
}