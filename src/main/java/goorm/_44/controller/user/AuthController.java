package goorm._44.controller.user;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.response.KakaoUserInfoResponse;
import goorm._44.entity.User;
import goorm._44.repository.UserRepository;
import goorm._44.service.user.KakaoService;
import goorm._44.common.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "로그인 및 토큰 관련 API")
public class AuthController {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/kakao/login")
    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드(code)로 Access/Refresh 토큰을 발급합니다.")
    public ApiResult<LoginTokenResponse> exchange(@RequestParam String code) {
        String kakaoAccess = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(kakaoAccess);

        User user = userRepository.findByName(userInfo.getKakaoAccount().getProfile().getNickName())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(userInfo.getKakaoAccount().getProfile().getNickName())
                                .password(String.valueOf(userInfo.getId())) // 임시 비번
                                .build()
                ));

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()));

        return ApiResult.success(new LoginTokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Access Token 재발급", description = "리프레시 토큰으로 새로운 Access Token을 발급합니다.")
    public ApiResult<AccessTokenResponse> refreshAccessToken(@RequestParam("refreshToken") String refreshToken) {
        String userId = jwtTokenProvider.extractSubject(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        return ApiResult.success(new AccessTokenResponse(newAccessToken));
    }

    public record LoginTokenResponse(String accessToken, String refreshToken) {}
    public record AccessTokenResponse(String accessToken) {}
}
