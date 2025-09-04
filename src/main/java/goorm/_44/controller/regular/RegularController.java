package goorm._44.controller.regular;


import goorm._44.config.api.ApiResult;
import goorm._44.dto.response.*;
import goorm._44.dto.request.NotiReadRequest;
import goorm._44.dto.request.StampRequest;
import goorm._44.service.regular.RegularService;
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

    @GetMapping("/{storeId}")
    @Operation(summary = "단골 여부 확인", description = "사용자가 특정 가게의 단골인지 확인합니다.")
    public ApiResult<Boolean> checkRegular(
            @PathVariable Long storeId,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        boolean isRegular = regularService.isRegular(userId, storeId);
        return ApiResult.success(isRegular);
    }

    @PostMapping("/{storeId}")
    @Operation(summary = "단골 등록", description = "사용자가 특정 가게의 단골이 됩니다.")
    public ApiResult<String> registerRegular(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        regularService.registerRegular(userId, storeId);
        return ApiResult.success("단골 등록이 완료되었습니다.");
    }

    @GetMapping("/main")
    @Operation(summary = "단골 가게 메인 조회", description = "내 단골 가게 목록과 새 공지 여부를 조회합니다.")
    public ApiResult<?> getMyRegularStores(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<RegularMainResponse> stores = regularService.getRegularStores(userId);

        return ApiResult.success(new Object() {
            public final int count = stores.size();
            public final List<RegularMainResponse> storeList = stores;
        });
    }

    @GetMapping("/{storeId}/detail")
    @Operation(summary = "단골 가게 상세 조회", description = "단골 가게 상세 정보와 최신 공지를 조회합니다.")
    public ApiResult<StoreDetailResponse> getStoreDetail(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(regularService.getStoreDetail(userId, storeId));
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
    @PostMapping("/store/stamp")
    @Operation(summary = "해당 가게의 스탬프 찍기 버튼입니다.")
    public ResponseEntity<String> registerRegularStore(@RequestBody StampRequest request, Authentication authentication) {
        // userId와 storeId를 요청 본문에서 직접 가져옵니다.
        Long storeId = request.getStoreId();
        Long userId = Long.parseLong(authentication.getName());
        // 서비스 로직 호출
        regularService.addStamp(storeId, userId);

        return ResponseEntity.ok("스탬프 찍기 완료");
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