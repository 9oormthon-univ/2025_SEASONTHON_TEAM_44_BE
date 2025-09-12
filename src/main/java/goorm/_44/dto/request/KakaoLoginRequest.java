package goorm._44.dto.request;

import goorm._44.entity.Role;

public record KakaoLoginRequest(
        String code,
        Role role
) {}