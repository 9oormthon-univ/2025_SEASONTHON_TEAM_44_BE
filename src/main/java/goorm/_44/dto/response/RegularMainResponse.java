package goorm._44.dto.response;

import java.time.LocalDateTime;

public record RegularMainResponse(
        Long storeId,
        String storeName,
        String imageUrl,
        LocalDateTime lastVisit,
        Integer visitCount,
        Integer availableStamp,
        boolean hasNewNoti
) {}