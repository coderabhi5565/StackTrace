package com.stacktrace.post_service.exception;

public class ImageUploadFailedException extends RuntimeException{

    public ImageUploadFailedException(String m){
        super(m);
    }
}
