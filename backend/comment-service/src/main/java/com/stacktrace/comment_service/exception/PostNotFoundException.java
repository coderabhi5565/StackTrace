package com.stacktrace.comment_service.exception;

public class PostNotFoundException extends RuntimeException{

    public PostNotFoundException(String s){
        super(s);
    }
}
