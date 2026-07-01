package br.com.creditengine.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Page response")
public record PageResponse<T>(
        @Schema(
                description = "List of elements",
                example = "[]"
        )
        List<T> content,
        @Schema(
                description = "Page number",
                example = "0"
        )
        int page,
        @Schema(
                description = "Page size",
                example = "10"
        )
        int size,
        @Schema(
                description = "Total elements",
                example = "20"
        )
        long totalElements,
        @Schema(
                description = "Total pages",
                example = "2"
        )
        int totalPages,
        @Schema(
                description = "First element to page",
                example = "true"
        )
        boolean first,
        @Schema(
                description = "Last element to page",
                example = "true"
        )
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

}
