package com.stacktrace.post_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SchedulePublishRequest {

    @NotNull(message = "Publish time is required")
    private Instant publishAt;

}