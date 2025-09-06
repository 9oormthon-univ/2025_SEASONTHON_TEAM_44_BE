package goorm._44.dto.response;

public record StoreResponse(
        Long id,
        String name,
        String imageUrl,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        String open,
        String close
) {}
