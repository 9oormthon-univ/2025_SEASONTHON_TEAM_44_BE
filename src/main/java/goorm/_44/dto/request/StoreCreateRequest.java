package goorm._44.dto.request;

import java.util.List;

public record StoreCreateRequest(
        String name,
        String imageKey,
        String introduction,
        String phone,
        String address,
        String detailAddress,
        Integer open,
        Integer close,
        String category,
        List<String> menuImageKeys
) {}