package groom._55.dto.response;

public record StoreResponse(
        String name,
        String imageUrl,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        Integer open,
        Integer close
) {}