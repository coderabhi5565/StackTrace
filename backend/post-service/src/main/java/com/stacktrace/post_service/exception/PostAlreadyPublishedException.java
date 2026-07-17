package com.stacktrace.post_service.exception;


public class PostAlreadyPublishedException  extends RuntimeException{

    public PostAlreadyPublishedException(String s){
        super(s);
    }
}
