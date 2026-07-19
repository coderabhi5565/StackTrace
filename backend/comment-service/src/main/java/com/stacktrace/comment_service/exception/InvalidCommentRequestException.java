package com.stacktrace.comment_service.exception;

public class InvalidCommentRequestException extends RuntimeException{

    public InvalidCommentRequestException(String s){
        super(s);
    }
}
