package com.eventcollab.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApiError {
    private final int           status;
    private final String        error;
    private final String        message;
    private final List<String>  details;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}