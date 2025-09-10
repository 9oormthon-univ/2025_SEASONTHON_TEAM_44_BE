package goorm._44.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GeocodeResponse(
        @Schema(nullable = true)
        String address
) {}

