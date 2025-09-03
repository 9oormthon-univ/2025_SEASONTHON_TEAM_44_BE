package groom._55.dto;

public record StoreResponse(
        String name,
        String imageUrl,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        String category,
        Integer open,
        Integer close
) {}