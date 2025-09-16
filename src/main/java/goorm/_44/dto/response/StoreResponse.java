package goorm._44.dto.response;

import java.util.List;

public record StoreResponse(
        Long id,
        String name,
        String storeImageUrl,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        String open,
        String close,
        String category,
        List<String> menuImageUrls
) {}
