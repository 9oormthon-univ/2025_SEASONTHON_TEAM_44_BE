package groom._55.controller;

import groom._55.dto.response.KakaoUserInfoResponse;
import groom._55.entity.User;
import groom._55.repository.UserRepository;
import groom._55.service.KakaoService;
import groom._55.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final UserRepository userRepository; // UserRepository를 주입받아 사용
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 제공자를 주입받아 사용

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(accessToken);

        String nickname = userInfo.getKakaoAccount().getProfile().getNickName();

        // 닉네임 기준으로 사용자 조회
        User user = userRepository.findByName(nickname)
                .orElseGet(() -> {
                    // 없으면 회원가입
                    return userRepository.save(
                            User.builder()
                                    .name(nickname)
                                    .password(String.valueOf(userInfo.getId())) // 어차피 안쓸 값
                                    .build()
                    );
                });

        // userId를 subject로 해서 JWT 생성
        String jwtToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()));

        return ResponseEntity.ok(jwtToken);
    }
}