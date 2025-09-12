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

@Tag(name = "User-Kakao", description = "카카오 로그인 / 토큰 발급")
@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/exchange")
    @Operation(summary = "카카오 코드 교환", description = "카카오 인가 코드(code)로 액세스/리프레시 토큰을 발급합니다.")
    public ApiResult<LoginTokenResponse> exchange(@RequestParam String code) {
        String kakaoAccess = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(kakaoAccess);

        String nickname = userInfo.getKakaoAccount().getProfile().getNickName();
        User user = userRepository.findByName(nickname)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(nickname)
                                .password(String.valueOf(userInfo.getId())) // 임시 저장
                                .build()
                ));

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()));

        return ApiResult.success(new LoginTokenResponse(accessToken, refreshToken));
    }

    // 응답 DTO
    public record LoginTokenResponse(String accessToken, String refreshToken) {}
}
