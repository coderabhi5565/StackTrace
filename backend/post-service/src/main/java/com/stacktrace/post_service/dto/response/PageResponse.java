package com.stacktrace.post_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/** Stable page shape (same as user-service PagedResponse). */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(),
                p.getTotalPages(), p.isLast());
    }
}
