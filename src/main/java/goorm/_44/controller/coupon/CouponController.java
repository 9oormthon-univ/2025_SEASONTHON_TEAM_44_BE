package goorm._44.controller.coupon;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.request.CouponRequest;
import goorm._44.dto.response.CouponResponse;
import goorm._44.dto.response.IdResponse;
import goorm._44.service.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/me")
    @Operation(summary = "[사장] 내 가게 쿠폰 등록/수정", description = "사장님이 자신의 가게 쿠폰을 등록하거나 수정합니다.")
    public ApiResult<IdResponse> upsertCoupon(
            @RequestBody CouponRequest req,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Long couponId = couponService.upsertCoupon(userId, req);
        return ApiResult.success(new IdResponse(couponId));
    }

    @GetMapping("/me")
    @Operation(summary = "[사장] 내 가게 쿠폰 조회", description = "사장님이 자신의 가게 쿠폰을 조회합니다.")
    public ApiResult<CouponResponse> getMyCoupon(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(couponService.getMyCoupon(userId));
    }

}
