package groom._55.dto;

public record StoreCreateRequest(
        String name,
        String imageKey,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        String category,
        Integer open,
        Integer close
) {}