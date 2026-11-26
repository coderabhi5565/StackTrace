package com.StackTrace.User_Service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Internal only: points are awarded by other services for publishing, commenting, likes. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsDeltaRequest {
    @Min(-1000)
    @Max(1000)
    private int delta;
}
