package groom._55.controller;

import groom._55.dto.request.NotiCreateRequest;
import groom._55.dto.response.NotiDetailResponse;
import groom._55.service.NotiService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotiController {

    private final NotiService notiService;

    // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
    // TODO: 공지 발송 후 고객한테 보여지는 로직
    @Operation(summary = "공지 등록", description = "사장님이 가게의 공지를 등록합니다.")
    @PostMapping
    public ResponseEntity<Long> createNoti(@RequestBody NotiCreateRequest req) {
        return ResponseEntity.ok(notiService.createNoti(req));
    }

    // TODO: 고객의 메인홈 안읽은 공지 로직 필요
    // TODO: 공지 확인 로직 필요

    // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
    @Operation(summary = "안읽은 공지 단건 조회", description = "사용자에게 해당되며 아직 읽지 않은 공지를 반환합니다.")
    @GetMapping("/{notiId}")
    public ResponseEntity<NotiDetailResponse> getUnreadNoti(@PathVariable Long notiId) {
        Long userId = 1L; // 임시 하드코딩
        return ResponseEntity.ok(notiService.getUnreadNoti(userId, notiId));
    }
}

//    @Operation(summary = "공지 확인 처리", description = "사용자가 공지를 읽었을 때 확인 처리합니다.")
//    @PostMapping("/read")
//    public ResponseEntity<Void> markAsRead(@RequestBody NotiReadRequest req) {
//        notiService.markAsRead(req);
//        return ResponseEntity.ok().build();
//    }
//}
