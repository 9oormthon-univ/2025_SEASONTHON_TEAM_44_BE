package goorm._44.controller.user;

import goorm._44.common.api.ApiResult;
import goorm._44.common.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
@Tag(name = "User-Refresh", description = "리프레시 토큰으로 새 액세스 토큰 발급")
public class RefreshTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 재발급", description = "리프레시 토큰을 이용해 새로운 액세스 토큰을 발급합니다.")
    public ApiResult<AccessTokenResponse> refreshAccessToken(@RequestParam("refreshToken") String refreshToken) {
        String userId = jwtTokenProvider.extractSubject(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        return ApiResult.success(new AccessTokenResponse(newAccessToken));
    }

    // 응답 DTO
    public record AccessTokenResponse(String accessToken) {}
}
