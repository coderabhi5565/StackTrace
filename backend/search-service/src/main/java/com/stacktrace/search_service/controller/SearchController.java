package com.stacktrace.search_service.controller;

import com.stacktrace.search_service.SearchType;
import com.stacktrace.search_service.dto.response.SearchResponse;
import com.stacktrace.search_service.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/search", "/api/v1/search"})
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResponse search(
            @RequestParam @NotBlank(message = "Keyword cannot be empty") @Size(max = 200) String keyword,
            @RequestParam(defaultValue = "ALL") SearchType type,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }
        return searchService.search(keyword, type, tag, page, size);
    }
}
