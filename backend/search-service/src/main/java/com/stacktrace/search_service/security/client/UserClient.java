package com.stacktrace.search_service.security.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {
}
