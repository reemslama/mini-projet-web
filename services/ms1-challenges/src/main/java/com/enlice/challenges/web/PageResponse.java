package com.enlice.challenges.web;

import java.util.List;
import org.springframework.data.domain.Page;

/** Page explicite (le contrat n'expose pas la structure interne de Spring Data). */
public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int number, int size) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }
}
