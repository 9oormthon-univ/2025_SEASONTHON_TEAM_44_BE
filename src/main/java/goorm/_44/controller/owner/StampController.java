package goorm._44.controller.owner;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.response.PageResponse;
import goorm._44.dto.response.StampLogForOwnerResponse;
import goorm._44.service.owner.StampService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner/stamp")
@RequiredArgsConstructor
@Tag(name = "Owner-Visit", description = "방문·적립 관련 API")
public class StampController {

    private final StampService stampService;

    @GetMapping("/logs")
    @Operation(
            summary = "방문·적립 페이지 조회",
            description = "자신(현재 로그인한 사장님)의 가게 방문·적립 이력을 페이지네이션하여 조회합니다. page는 0부터 시작, 기본 size=9."
    )
    public ApiResult<PageResponse<StampLogForOwnerResponse>> getStampLogsForOwner(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(stampService.getStampLogsForOwner(userId, page, size));
    }
}
