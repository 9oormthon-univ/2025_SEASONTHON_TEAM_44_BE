package goorm._44.controller.noti;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.request.NotiCreateRequest;
import goorm._44.dto.response.IdResponse;
import goorm._44.dto.response.NotiLogResponse;
import goorm._44.dto.response.PageResponse;
import goorm._44.service.noti.NotiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notis")
@RequiredArgsConstructor
@Tag(name = "Noti", description = "공지 관련 API")
public class NotiController {

    private final NotiService notiService;

    @Operation(summary = "[사장] 공지 등록", description = "사용자의 가게 공지를 등록합니다.")
    @PostMapping
    public ApiResult<IdResponse> createNoti(@RequestBody NotiCreateRequest req,
                                            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Long notiId = notiService.createNoti(req, userId);
        return ApiResult.success(new IdResponse(notiId));
    }


    @Operation(summary = "[사장] 공지 로그 조회", description = "사용자가 등록한 공지 로그를 조회합니다. page는 0부터 시작합니다.")
    @GetMapping("/logs")
    public ApiResult<PageResponse<NotiLogResponse>> getNotiLogs(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(notiService.getNotiLogs(userId, page, size));
    }


    @PostMapping("/{notiId}/read")
    @Operation(summary = "[단골] 공지 읽기", description = "사용자가 특정 공지를 읽습니다.")
    public ApiResult<IdResponse> readNoti(
            @PathVariable Long notiId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        notiService.readNoti(userId, notiId);
        return ApiResult.success(new IdResponse(notiId));
    }
}
