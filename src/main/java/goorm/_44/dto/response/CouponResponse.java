package goorm._44.dto.response;

public record CouponResponse(
        Long id,
        String name,
        String benefit,
        int requiredStamp
) {}
