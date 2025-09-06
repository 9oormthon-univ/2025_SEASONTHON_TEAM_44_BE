package goorm._44.controller.user;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.UserLocationRequest;
import goorm._44.dto.response.UserSimpleResponse;
import goorm._44.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/location")
    @Operation(summary = "위치 등록 여부 확인", description = "현재 로그인한 사용자가 위치를 등록했는지 여부를 반환합니다.")
    public ApiResult<Boolean> isLocationRegistered(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        boolean registered = userService.isLocationRegistered(userId);
        return ApiResult.success(registered);
    }

    @PostMapping("/location")
    @Operation(summary = "위치 등록", description = "현재 로그인한 사용자의 주소를 등록합니다.")
    public ApiResult<String> registerLocation(
            @RequestBody UserLocationRequest req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.updateLocation(userId, req.region());
        return ApiResult.success("위치 등록이 완료되었습니다.");
    }

    @GetMapping("/me/simple")
    @Operation(summary = "내 정보 간단 조회", description = "현재 로그인한 사용자의 이름, 프로필 이미지, 지역을 조회합니다.")
    public ApiResult<UserSimpleResponse> getMyInfo(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(userService.getUserInfo(userId));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴(삭제)합니다.")
    public ApiResult<String> deleteMe(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        userService.deleteUser(userId);
        return ApiResult.success("회원 탈퇴가 완료되었습니다.");
    }

}
