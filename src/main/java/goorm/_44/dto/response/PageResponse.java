package goorm._44.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,          // 0-based
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}
