package goorm._44.controller.user;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.UserLocationRequest;
import goorm._44.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/user/location")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "위치 등록", description = "현재 로그인한 사용자의 주소를 등록합니다.")
    public ApiResult<String> registerLocation(
            @RequestBody UserLocationRequest req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.updateLocation(userId, req.region());
        return ApiResult.success("위치 등록이 완료되었습니다.");
    }
}
