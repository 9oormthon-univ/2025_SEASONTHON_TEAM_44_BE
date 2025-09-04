package goorm._44.controller.user;

import goorm._44.utils.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
@Tag(name = "User-Refresh")
public class RefreshTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam("refreshToken") String refreshToken) {
        try {
            // refreshToken 유효성 검증
            if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(401).body("Refresh token expired. Please login again.");
            }

            // 토큰 subject (유저 ID) 추출
            String userId = jwtTokenProvider.extractSubject(refreshToken);

            // Refresh Token 검증 (tokenType이 refreshToken 인지 확인해도 좋음)
            String tokenType = jwtTokenProvider.extractClaim(refreshToken, claims -> (String) claims.get("tokenType"));
            if (!"refreshToken".equals(tokenType)) {
                return ResponseEntity.status(400).body("Invalid token type. Refresh token required.");
            }

            // 새 Access Token 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(userId);

            return ResponseEntity.ok(newAccessToken);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }
}