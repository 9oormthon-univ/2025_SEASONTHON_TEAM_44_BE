package goorm._44.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignResponse(
        @Schema(description = "S3 객체 키", example = "uploads/2025/09/uuid_logo.png")
        String key,
        @Schema(description = "PUT 업로드용 프리사인 URL")
        String url,
        @Schema(description = "만료 시각(Unix epoch ms)", example = "1756889041070")
        long expiresAt
) {}
