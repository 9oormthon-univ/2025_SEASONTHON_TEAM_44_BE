package goorm._44.dto.response;

import java.time.LocalDateTime;

public record StoreDetailResponse(
        Long storeId,
        String storeName,
        String introduction,
        String phone,          // ✅ 포맷된 문자열
        String address,
        String detailAddress,
        String open,           // ✅ "HH:mm"
        String close,          // ✅ "HH:mm"
        String imageUrl,
        Integer availableStamp,
        NotiSimpleResponse latestNoti
) {
    public record NotiSimpleResponse(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt
    ) {}
}
