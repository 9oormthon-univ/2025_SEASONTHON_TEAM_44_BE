package goorm._44.config.security;

import goorm._44.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        // 토큰이 존재하고, SecurityContext에 인증 정보가 없는 경우에만 검증 로직 실행
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtTokenProvider.extractSubject(token);

                // 핵심 수정: 'extractSubject' 대신 'validateToken'을 사용해 토큰의 유효성을 완전히 검사합니다.
                // 여기서는 토큰의 subject를 기준으로 유효성을 검증합니다.
                // 실제로는 데이터베이스에서 사용자 정보를 로드하고 토큰의 subject와 비교하는 로직이 필요합니다.
                // 예제에서는 단순화를 위해 subject만 검증합니다.
                if (jwtTokenProvider.validateToken(token, username)) {
                    UserDetails userDetails = new User(username, "", Collections.emptyList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 (만료, 변조 등)
                // SecurityContext에 인증 정보를 설정하지 않고 다음 필터로 넘어갑니다.
                // 결과적으로 인증이 실패하여 403 Forbidden 응답을 받게 됩니다.
                logger.error("JWT authentication failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
