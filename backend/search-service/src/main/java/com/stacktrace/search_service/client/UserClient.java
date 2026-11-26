package com.stacktrace.search_service.client;

import com.stacktrace.search_service.dto.response.PageResponse;
import com.stacktrace.search_service.dto.response.UserSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/internal/users/search")
    PageResponse<UserSearchResponse> searchUsers(@RequestParam("keyword") String keyword,
                                                 @RequestParam("page") int page,
                                                 @RequestParam("size") int size);
}
