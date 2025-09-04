package groom._55.controller;


import groom._55.dto.RegularStoreDetail;
import groom._55.dto.request.NotiReadRequest;
import groom._55.dto.request.StampRequest;
import groom._55.dto.response.CouponResponse;
import groom._55.dto.response.MyPageResponse;
import groom._55.dto.response.RegularMainResponse;
import groom._55.service.RegularService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regular")
@Tag(name = "Regular(단골)", description = "단골 관련 API")
@RequiredArgsConstructor // 의존성 주입을 위한 Lombok 어노테이션
public class RegularController {

    private final RegularService regularService;

    // GET /regular/store/detail/{storeId}
    @GetMapping("/store/detail/{storeId}")
    @Operation(summary = "가게 상세보기", description = "가게의 주소, 전화번호, 소개글 등이 포함되어 있는 가게의 자세 정보 페이지입니다. 가장 최근의 공지를 가져오고 읽음 여부는 hasNewNoti로 True/false로 구분합니다.")
    public ResponseEntity<RegularStoreDetail> getRegularStoreDetail(@PathVariable Long storeId, Authentication authentication) {
        // 실제 유저 ID는 세션, JWT 토큰 등에서 가져와야 합니다.
        Long userId = Long.parseLong(authentication.getName());
        RegularStoreDetail detail = regularService.getDetail(userId, storeId);
        return ResponseEntity.ok(detail);
    }

    // POST /regular/noti/read
    @PostMapping("/noti/read")
    @Operation(summary="공지 읽음 버튼", description = "전송 시 해당 공지를 읽음 처리합니다.")
    public ResponseEntity<String> markNotiAsRead(@RequestBody NotiReadRequest request, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        regularService.readNoti(userId, request.getNotiId());
        return ResponseEntity.ok("success");
    }

    // POST /regular/store/detail
    //    해당 함수는 만약 Stamp 데이터베이스에 등록이 안되어있다면 오류 발생
    //    즉 만약 단골 등록이 안되어있는 상태로 스탬프 찍으면 서버 에러 발생
    @PostMapping("/store/detail")
    @Operation(summary = "해당 가게의 스탬프 찍기 버튼입니다.")
    public ResponseEntity<String> registerRegularStore(@RequestBody StampRequest request, Authentication authentication) {
        // userId와 storeId를 요청 본문에서 직접 가져옵니다.
        Long storeId = request.getStoreId();
        Long userId = Long.parseLong(authentication.getName());
        // 서비스 로직 호출
        regularService.addStamp(storeId, userId);

        return ResponseEntity.ok("스탬프 찍기 완료");
    }

    @GetMapping("/main")
    @Operation(summary = "단골 메인홈 페이지 입니다.", description = "방문한지 오래된 순서로 전달합니다.")
    public ResponseEntity<List<RegularMainResponse>> getRegularMain(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<RegularMainResponse> response = regularService.getRegularStores(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/coupon")
    @Operation(summary = "쿠폰함 페이지 입니다.", description = "쿠폰이 몇개 있는지 확인 가능합니다.")
    public ResponseEntity<List<CouponResponse>> getCoupons(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<CouponResponse> response = regularService.getCoupons(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/coupon/{stampId}")
    @Operation(summary = "쿠폰함 사용 버튼입니다.")
    public ResponseEntity<String> useCoupon(@PathVariable Long stampId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        regularService.useCoupon(userId, stampId);
        return ResponseEntity.ok("쿠폰 사용 완료 (10 스탬프 차감)");
    }

    @GetMapping("/mypage")
    @Operation(summary = "나의 정보들을 혹인할 수 있는 페이지입니다.")
    public ResponseEntity<MyPageResponse> getMyPage(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        MyPageResponse response = regularService.getMyPage(userId);
        return ResponseEntity.ok(response);
    }
}