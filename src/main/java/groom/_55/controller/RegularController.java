package groom._55.controller;


import groom._55.dto.*;
import groom._55.dto.request.NotiReadRequest;
import groom._55.dto.request.StampRequest;
import groom._55.dto.response.CouponResponse;
import groom._55.dto.response.MyPageResponse;
import groom._55.dto.response.RegularMainResponse;
import groom._55.service.RegularService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regular")
@RequiredArgsConstructor // 의존성 주입을 위한 Lombok 어노테이션
public class RegularController {

    private final RegularService regularService;

    // GET /regular/store/detail/{storeId}
    @GetMapping("/store/detail/{storeId}")
    public ResponseEntity<RegularStoreDetail> getRegularStoreDetail(@PathVariable Long storeId) {
        // 실제 유저 ID는 세션, JWT 토큰 등에서 가져와야 합니다.
         Long userId = 1L; //임시 1번 유저 사용
        RegularStoreDetail detail = regularService.getDetail(userId, storeId);
        return ResponseEntity.ok(detail);
    }

    // POST /regular/noti/read
    @PostMapping("/noti/read")
    public ResponseEntity<String> markNotiAsRead(@RequestBody NotiReadRequest request) {
        Long userId = 1L; // 이 부분은 실제 서비스에서는 인증 로직으로 대체되어야 합니다.

        regularService.readNoti(userId, request.getNotiId());
        return ResponseEntity.ok("success");
    }

    // POST /regular/store/detail
    //    해당 함수는 만약 Stamp 데이터베이스에 등록이 안되어있다면 오류 발생
    //    즉 만약 단골 등록이 안되어있는 상태로 스탬프 찍으면 서버 에러 발생
    @PostMapping("/store/detail")
    public ResponseEntity<String> registerRegularStore(@RequestBody StampRequest request) {
        // userId와 storeId를 요청 본문에서 직접 가져옵니다.
        Long storeId = request.getStoreId();
        Long userId = request.getUserId();

        // 서비스 로직 호출
        regularService.addStamp(storeId, userId);

        return ResponseEntity.ok("스탬프 찍기 완료");
    }

    @GetMapping("/main")
    public ResponseEntity<List<RegularMainResponse>> getRegularMain() {
        Long userId = 1L; // 추후 인증 로직으로 대체
        List<RegularMainResponse> response = regularService.getRegularStores(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/coupon")
    public ResponseEntity<List<CouponResponse>> getCoupons() {
        Long userId = 1L; // 추후 인증 로직으로 교체
        List<CouponResponse> response = regularService.getCoupons(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/coupon/{stampId}")
    public ResponseEntity<String> useCoupon(@PathVariable Long stampId) {
        Long userId = 1L; // 추후 인증 로직으로 교체
        regularService.useCoupon(userId, stampId);
        return ResponseEntity.ok("쿠폰 사용 완료 (10 스탬프 차감)");
    }

    @GetMapping("/mypage")
    public ResponseEntity<MyPageResponse> getMyPage() {
        Long userId = 1L; // 추후 인증 로직으로 교체
        MyPageResponse response = regularService.getMyPage(userId);
        return ResponseEntity.ok(response);
    }
}