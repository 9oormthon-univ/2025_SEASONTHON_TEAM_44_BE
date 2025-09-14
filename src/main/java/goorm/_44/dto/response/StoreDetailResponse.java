package goorm._44.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StoreDetailResponse(
        Long storeId,
        String storeName,
        String introduction,
        String phone,          // 포맷된 문자열
        String address,
        String detailAddress,
        String open,           // "HH:mm"
        String close,          // "HH:mm"
        String storeImageUrl,              // 가게 대표 이미지
        List<String> menuImageUrls,        // 메뉴판 이미지 리스트
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
