package goorm._44.config.security;

import goorm._44.utils.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 활성화
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS는 별도의 WebConfig에서 설정하므로 SecurityConfig에서는 비활성화
                .cors(cors -> {})
                // CSRF 공격 방지 비활성화 (JWT는 세션을 사용하지 않으므로 CSRF 보호가 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)
                // 폼 기반 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션 관리 비활성화 (JWT는 stateless 방식이므로)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // '/api/login' 경로는 인증 없이 접근 가능
                        .requestMatchers("/api/login", "/login/page", "/callback", "/favicon.ico"
                        , "/swagger-ui/**", "/v3/api-docs/**", "/geocode/address", "auth/**", "/owner/**", "/owner",
                        "www.dasion.store/**", "www.dasion.store").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                // 이렇게 하면 요청이 올 때마다 JWT 필터가 먼저 실행되어 토큰을 검증합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
