package com.stacktrace.search_service.client;

import com.stacktrace.search_service.dto.response.UserSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;


@FeignClient(
        name = "user-service",
        url = "${user-service.url}"
)
public interface UserClient {

    @GetMapping("/internal/users/search")
    List<UserSearchResponse> searchUsers(
            @RequestParam String keyword
    );
}