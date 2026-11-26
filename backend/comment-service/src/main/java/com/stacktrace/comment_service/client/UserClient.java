package com.stacktrace.comment_service.client;

import com.stacktrace.comment_service.dto.response.AuthorSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(
        name = "user-service",
        url = "${user-service.url}"
)
public interface UserClient {

    @GetMapping("/internal/users/batch")
    List<AuthorSummary> batch(@RequestParam("ids") Collection<Long> ids);
}
