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

@Tag(name = "User-Kakao")
@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
public class KakaoLoginController {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<Map<String, String>> exchange(@RequestParam String code) {
        String kakaoAccess = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(kakaoAccess);

        String nickname = userInfo.getKakaoAccount().getProfile().getNickName();
        User user = userRepository.findByName(nickname)
                .orElseGet(() -> userRepository.save(
                        User.builder().name(nickname).password(String.valueOf(userInfo.getId())).build()
                ));

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()));
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()));

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }
}