package goorm._44.dto.response;

import java.time.LocalDateTime;

public record NotiLogResponse(
        Long id,
        String title,
        String target,
        Integer targetCount,  // 수신 인원
        Integer readCount,    // 열람 수
        LocalDateTime createdAt,
        String content
) {}