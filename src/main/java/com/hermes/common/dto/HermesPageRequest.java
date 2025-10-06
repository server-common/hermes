package com.hermes.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public record HermesPageRequest(
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    Integer size,

    String sortBy,
    String sortDir
) {

    public HermesPageRequest {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 20;
        }

        if (sortBy == null) {
            sortBy = "createdAt";
        }

        if (sortDir == null) {
            sortDir = "desc";
        }
    }

    public PageRequest toPageable() {
        return PageRequest.of(page, size, sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
    }
}