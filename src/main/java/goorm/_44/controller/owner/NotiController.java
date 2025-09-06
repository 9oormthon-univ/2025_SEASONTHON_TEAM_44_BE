package goorm._44.controller.owner;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.NotiCreateRequest;
import goorm._44.dto.response.NotiDetailResponse;
import goorm._44.dto.response.NotiLogResponse;
import goorm._44.dto.response.PageResponse;
import goorm._44.service.owner.NotiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner/noti")
@RequiredArgsConstructor
@Tag(name = "Owner-Noti", description = "공지 관련 API")
public class NotiController {

    private final NotiService notiService;

    @Operation(summary = "공지 등록", description = "사장님이 가게의 공지를 등록합니다.")
    @PostMapping
    public ResponseEntity<Long> createNoti(@RequestBody NotiCreateRequest req,
                                           Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(notiService.createNoti(req, userId));
    }

    @Operation(summary = "공지 로그 조회", description = "사장님이 발송한 공지 이력을 페이지네이션하여 조회합니다. page는 0부터 시작, 기본 size=9")
    @GetMapping("/logs")
    public ApiResult<PageResponse<NotiLogResponse>> getNotiLogs(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(notiService.getNotiLogs(userId, page, size));
    }
}
//    @Operation(summary = "안읽은 공지 단건 조회", description = "사용자에게 해당되며 아직 읽지 않은 공지를 반환합니다. 이미 읽었거나 대상이 아니면 null을 반환합니다.")
//    @GetMapping("/{notiId}")
//    public ResponseEntity<NotiDetailResponse> getUnreadNoti(@PathVariable Long notiId,
//                                                            Authentication authentication) {
//        Long userId = Long.parseLong(authentication.getName());
//        return ResponseEntity.ok(notiService.getUnreadNoti(userId, notiId));
//    }

//    @Operation(summary = "공지 확인 처리", description = "사용자가 공지를 읽었을 때 확인 처리합니다.")
//    @PostMapping("/read")
//    public ResponseEntity<Void> markAsRead(@RequestBody NotiReadRequest req) {
//        notiService.markAsRead(req);
//        return ResponseEntity.ok().build();
//    }
//}
