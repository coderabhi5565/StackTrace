package com.stacktrace.post_service.service.impl;

import com.stacktrace.post_service.dto.request.CreatePostRequest;
import com.stacktrace.post_service.dto.response.PostResponse;
import com.stacktrace.post_service.repository.PostRepository;
import com.stacktrace.post_service.repository.TagRepository;
import com.stacktrace.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        return null;
    }
}