package com.stacktrace.post_service.exception;

public class PostNotScheduledException extends RuntimeException {

    public PostNotScheduledException(String message) {
        super(message);
    }

}