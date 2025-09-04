package groom._55.dto.request;

import groom._55.entity.NotiTarget;

public record NotiCreateRequest(
        String title,
        String content,
        NotiTarget target // ALL | BASIC | CERTIFIED
) {}