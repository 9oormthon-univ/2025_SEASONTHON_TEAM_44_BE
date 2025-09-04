package groom._55.dto.response;

import java.time.LocalDateTime;

public record StampLogForOwnerResponse(
        LocalDateTime dateTime,
        String customerName,
        String action,     // "신규 등록" | "방문 적립" (현재 쿠폰 구분 불가)
        int cumulative,    // 해당 시점까지 누적
        String note        // 신규 등록일 때 "신규 단골 등록", 아니면 null
) {}

