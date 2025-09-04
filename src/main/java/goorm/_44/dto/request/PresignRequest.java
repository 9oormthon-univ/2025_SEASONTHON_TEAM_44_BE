package goorm._44.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignRequest(
        @Schema(description = "원본 파일명(표시용)", example = "logo.png")
        String fileName,
        @Schema(description = "MIME 타입", example = "image/png")
        String contentType
) {}

