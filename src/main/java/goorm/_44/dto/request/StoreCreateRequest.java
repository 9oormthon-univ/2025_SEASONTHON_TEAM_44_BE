package goorm._44.dto.request;

public record StoreCreateRequest(
        String name,
        String imageKey,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        Integer open,
        Integer close
) {}