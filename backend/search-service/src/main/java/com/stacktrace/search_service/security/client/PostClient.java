package com.stacktrace.search_service.security.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "post-service", url = "${post-service.url}")
public interface PostClient {
}
