package groom._55.dto.response;

import groom._55.entity.Noti;
import groom._55.entity.NotiTarget;

import java.time.LocalDateTime;

public record NotiDetailResponse(
        Long id,
        String title,
        String content,
        NotiTarget target,
        int targetCount,
        LocalDateTime createdAt
) {
    public static NotiDetailResponse from(Noti noti) {
        return new NotiDetailResponse(
                noti.getId(),
                noti.getTitle(),
                noti.getContent(),
                noti.getTarget(),
                noti.getTargetCount(),
                noti.getCreatedAt()
        );
    }
}