package goorm._44.dto.request;

import goorm._44.enums.NotiTarget;

public record NotiCreateRequest(
        String title,
        String content,
        NotiTarget target // ALL | BASIC | CERTIFIED
) {}