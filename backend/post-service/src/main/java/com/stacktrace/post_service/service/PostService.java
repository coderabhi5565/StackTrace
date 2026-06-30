package com.stacktrace.post_service.service;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;

public interface PostService {

    PostResponse createPost(CreatePostRequest request);

}