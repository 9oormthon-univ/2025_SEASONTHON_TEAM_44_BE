package goorm._44.controller.stamp;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.response.*;
import goorm._44.service.stamp.StampService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
@Tag(name = "Stamp", description = "스탬프 관련 API")
public class StampController {

    private final StampService stampService;


    @GetMapping("/logs")
    @Operation(
            summary = "[사장] 방문 적립 로그 조회",
            description = "사용자 가게의 방문 및 적립 로그를 조회합니다. page는 0부터 시작합니다."
    )
    public ApiResult<PageResponse<StampLogResponse>> getStampLogs(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(stampService.getStampLogs(userId, page, size));
    }




    @GetMapping("/me/main")
    @Operation(summary = "[단골] 단골 가게 메인 조회", description = "내 단골 가게 목록과 새 공지 여부를 조회합니다. 스탬프 임박순으로 조회됩니다.")
    public ApiResult<RegularMainListResponse> getMyRegularStores(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<RegularMainResponse> stores = stampService.getRegularStores(userId);
        return ApiResult.success(new RegularMainListResponse(stores.size(), stores));
    }


    @GetMapping("me/stores/{storeId}")
    @Operation(summary = "[단골] 단골 가게 상세 조회", description = "단골 가게 상세 정보와 최신 공지를 조회합니다.")
    public ApiResult<StoreDetailResponse> getStoreDetail(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(stampService.getStoreDetail(userId, storeId));
    }


    @GetMapping("/me/summary")
    @Operation(summary = "[단골] 내 단골 활동 요약 조회 (=마이페이지)", description = "단골 가게 수, 보유 스탬프/쿠폰 수, 최근 방문 가게 목록을 조회합니다.")
    public ApiResult<MyPageResponse> getMyPage(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        MyPageResponse response = stampService.getMyPage(userId);
        return ApiResult.success(response);
    }


    @GetMapping("/coupons")
    @Operation(summary = "[단골] 쿠폰 목록 조회", description = "보유 중인 쿠폰 목록을 조회합니다.")
    public ApiResult<List<CouponResponse>> getCoupons(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(stampService.getCoupons(userId));
    }


    @PostMapping("/{stampId}/coupon")
    @Operation(summary = "[단골] 쿠폰 사용", description = "해당 스탬프 ID로 쿠폰을 사용합니다. (10 스탬프 차감)")
    public ApiResult<IdResponse> useStamp(
            @PathVariable Long stampId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        stampService.useStamp(userId, stampId);
        return ApiResult.success(new IdResponse(stampId));
    }

    public record RegularMainListResponse(int count, List<RegularMainResponse> storeList) {}


}
