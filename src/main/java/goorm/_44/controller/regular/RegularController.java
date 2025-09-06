package goorm._44.controller.regular;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.NotiReadRequest;
import goorm._44.dto.request.StampRequest;
import goorm._44.dto.response.CouponResponse;
import goorm._44.dto.response.MyPageResponse;
import goorm._44.dto.response.RegularMainResponse;
import goorm._44.dto.response.StoreDetailResponse;
import goorm._44.service.regular.RegularService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regular")
@Tag(name = "Regular(단골)", description = "단골 관련 API")
@RequiredArgsConstructor
public class RegularController {

    private final RegularService regularService;

    @GetMapping("/{storeId}")
    @Operation(summary = "단골 여부 확인", description = "사용자가 특정 가게의 단골인지 확인합니다.")
    public ApiResult<Boolean> checkRegular(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
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
    @Operation(summary = "단골 가게 메인 조회", description = "내 단골 가게 목록과 새 공지 여부를 조회합니다. 스탬프 임박순으로 조회됩니다.")
    public ApiResult<RegularMainListResponse> getMyRegularStores(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<RegularMainResponse> stores = regularService.getRegularStores(userId);
        return ApiResult.success(new RegularMainListResponse(stores.size(), stores));
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

    @PostMapping("/noti/read")
    @Operation(summary = "공지 읽음 처리", description = "요청한 공지를 읽음 처리합니다.")
    public ApiResult<String> markNotiAsRead(
            @RequestBody NotiReadRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        regularService.readNoti(userId, request.getNotiId());
        return ApiResult.success("success");
    }

    // 만약 단골 등록(Stamp) 엔티티가 없으면 서비스에서 예외 발생하도록 설계
    @PostMapping("/store/stamp")
    @Operation(summary = "스탬프 적립", description = "해당 가게에 스탬프를 1회 적립합니다.")
    public ApiResult<String> registerRegularStore(
            @RequestBody StampRequest request,
            Authentication authentication
    ) {
        Long storeId = request.getStoreId();
        Long userId = Long.parseLong(authentication.getName());
        regularService.addStamp(storeId, userId);
        return ApiResult.success("스탬프 찍기 완료");
    }

    @GetMapping("/coupon")
    @Operation(summary = "쿠폰함 조회", description = "보유 중인 쿠폰 목록을 조회합니다.")
    public ApiResult<List<CouponResponse>> getCoupons(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<CouponResponse> response = regularService.getCoupons(userId);
        return ApiResult.success(response);
    }

    @PostMapping("/coupon/{stampId}")
    @Operation(summary = "쿠폰 사용", description = "해당 스탬프 ID로 쿠폰을 사용합니다. (10 스탬프 차감)")
    public ApiResult<String> useCoupon(
            @PathVariable Long stampId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        regularService.useCoupon(userId, stampId);
        return ApiResult.success("쿠폰 사용 완료 (10 스탬프 차감)");
    }

    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 조회", description = "나의 정보들을 확인할 수 있는 페이지입니다.")
    public ApiResult<MyPageResponse> getMyPage(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        MyPageResponse response = regularService.getMyPage(userId);
        return ApiResult.success(response);
    }

    // 응답 통일을 위한 작은 DTO (리스트 + 카운트)
    public record RegularMainListResponse(int count, List<RegularMainResponse> storeList) {}
}
