package groom._55.dto.response;

public record StoreResponse(
        Long id,
        String name,
        String imageUrl,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        Integer open,
        Integer close
) {}