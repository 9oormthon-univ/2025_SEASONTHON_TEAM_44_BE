package goorm._44.dto.response;

import java.time.LocalDateTime;

public record StoreDetailResponse(
        Long storeId,
        String storeName,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        Integer open,
        Integer close,
        String imageUrl,
        Integer availableStamp,
        NotiSimpleResponse latestNoti
) {
    // 내부 record
    public record NotiSimpleResponse(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt
    ) {}
}
