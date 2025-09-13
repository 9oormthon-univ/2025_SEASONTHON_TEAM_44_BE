package goorm._44.controller.store;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.request.StoreCreateRequest;
import goorm._44.dto.response.DashboardResponse;
import goorm._44.dto.response.IdResponse;
import goorm._44.dto.response.StoreResponse;
import goorm._44.service.insight.InsightService;
import goorm._44.service.store.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "가게 관련 API")
public class StoreController {
    private final StoreService storeService;
    private final InsightService insightService;

    @GetMapping("/me/exists")
    @Operation(summary = "[사장] 가게 등록 여부 확인", description = "사용자의 가게 등록 여부를 확인합니다.")
    public ApiResult<Boolean> existsMyStore(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(storeService.existsMyStore(userId));
    }

    @PostMapping
    @Operation(summary = "[사장] 가게 등록", description = "사용자의 가게 정보를 등록합니다.")
    public ApiResult<IdResponse> createStore(
            @RequestBody StoreCreateRequest req,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Long storeId = storeService.createStore(req, userId);
        return ApiResult.success(new IdResponse(storeId));
    }


    @GetMapping("/me")
    @Operation(summary = "[사장] 내 가게 조회", description = "사용자의 가게 정보를 조회합니다.")
    public ApiResult<StoreResponse> getMyStore(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(storeService.getMyStore(userId));
    }


    @GetMapping("/me/summary")
    @Operation(summary = "[사장] 내 대시보드 조회", description = "사용자의 가게 대시보드 데이터를 조회합니다.")
    public ApiResult<DashboardResponse> getMyDashboard(Authentication authentication) {
        Long ownerUserId = Long.parseLong(authentication.getName());
        return ApiResult.success(storeService.getMyDashboard(ownerUserId));
    }




    @GetMapping("/{storeId}/regular")
    @Operation(summary = "[단골] 단골 여부 확인", description = "사용자의 특정 가게 단골 여부를 확인합니다.")
    public ApiResult<Boolean> hasStamp(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isRegular = storeService.hasStamp(userId, storeId);
        return ApiResult.success(isRegular);
    }


    @PostMapping("/{storeId}/register")
    @Operation(summary = "[단골] 단골 등록", description = "사용자가 특정 가게의 단골로 등록됩니다.")
    public ApiResult<IdResponse> registerStamp(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Long regularId = storeService.registerStamp(userId, storeId);
        return ApiResult.success(new IdResponse(regularId));
    }


    @PostMapping("/{storeId}/visit")
    @Operation(summary = "[단골] 스탬프 적립", description = "해당 가게에 스탬프를 1회 적립합니다.")
    public ApiResult<IdResponse> addStamp(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Long stampId = storeService.addStamp(userId, storeId);
        return ApiResult.success(new IdResponse(stampId));
    }

    @PostMapping("/insight")
    @Operation(summary="[사장] 인사이트 제공", description = "전일 대비 인사이트를 제공합니다.")
    public ApiResult<String> insight(
        Authentication authentication
    ) {
        Long ownerUserId = Long.parseLong(authentication.getName());
        String insightResult = insightService.getInsight(ownerUserId);
        return ApiResult.success("insight");
    }
}