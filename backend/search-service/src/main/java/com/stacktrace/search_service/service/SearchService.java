package com.stacktrace.search_service.service;

import com.stacktrace.search_service.SearchType;
import com.stacktrace.search_service.dto.response.SearchResponse;

public interface SearchService {
    SearchResponse search(String keyword, SearchType type);
}
