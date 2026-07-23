package com.stacktrace.search_service.controller;

import com.stacktrace.search_service.SearchType;
import com.stacktrace.search_service.dto.response.SearchResponse;
import com.stacktrace.search_service.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam @NotBlank(message = "Keyword cannot be empty") String keyword,
            @RequestParam(defaultValue = "ALL") SearchType type
    ) {
        return ResponseEntity.ok(
                searchService.search(keyword, type)
        );
    }
}