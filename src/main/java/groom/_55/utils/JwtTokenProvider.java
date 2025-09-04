package groom._55.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    // 토큰 만료 시간
    private static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofHours(1);      // 1시간
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);      // 7일

    /**
     * 서명 키 반환
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String subject) {
        return createToken(subject, ACCESS_TOKEN_VALIDITY, Map.of("tokenType", "accessToken"));
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String subject) {
        return createToken(subject, REFRESH_TOKEN_VALIDITY, Map.of("tokenType", "refreshToken"));
    }

    /**
     * JWT 생성 (공통 로직)
     */
    private String createToken(String subject, Duration validity, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity.toMillis());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰에서 사용자 식별자(subject) 추출
     */
    public String extractSubject(String token) {
        return extractClaim(token, claim -> claim.getSubject());
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, claim -> claim.getExpiration());
        return expiration.before(new Date());
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token, String expectedSubject) {
        String actualSubject = extractSubject(token);
        return actualSubject.equals(expectedSubject) && !isTokenExpired(token);
    }

    /**
     * 특정 Claim 추출
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 전체 Claims 추출 (내부 전용)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
