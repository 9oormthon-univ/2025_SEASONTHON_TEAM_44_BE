package goorm._44.controller.user;

import goorm._44.dto.response.KakaoUserInfoResponse;
import goorm._44.entity.User;
import goorm._44.repository.UserRepository;
import goorm._44.service.user.KakaoService;
import goorm._44.utils.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "User-Kakao")
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final UserRepository userRepository; // UserRepository를 주입받아 사용
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 제공자를 주입받아 사용

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        String accessTokenFromKakao = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(accessTokenFromKakao);

        String nickname = userInfo.getKakaoAccount().getProfile().getNickName();

        // 닉네임 기준으로 사용자 조회 or 회원가입
        User user = userRepository.findByName(nickname)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(nickname)
                                .password(String.valueOf(userInfo.getId()))
                                .build()
                ));

        // JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()));

        // 액세스 + 리프레시 토큰 반환
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }
}