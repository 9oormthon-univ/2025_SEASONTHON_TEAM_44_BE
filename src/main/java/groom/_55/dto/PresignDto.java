package groom._55.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PresignDto {

    public record PresignRequest(
            @Schema(description = "원본 파일명(표시용)", example = "logo.png")
            String fileName,
            @Schema(description = "MIME 타입", example = "image/png")
            String contentType
    ) {}

    public record PresignResponse(
            @Schema(description = "S3 객체 키", example = "uploads/2025/09/uuid_logo.png")
            String key,
            @Schema(description = "PUT 업로드용 프리사인 URL")
            String url,
            @Schema(description = "만료 시각(Unix epoch ms)", example = "1756889041070")
            long expiresAt
    ) {}
}
