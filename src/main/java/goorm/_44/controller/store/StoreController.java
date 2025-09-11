package goorm._44.controller.store;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.StoreCreateRequest;
import goorm._44.dto.response.DashboardResponse;
import goorm._44.dto.response.IdResponse;
import goorm._44.dto.response.StoreResponse;
import goorm._44.service.store.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "가게 관련 API")
public class StoreController {
    private final StoreService storeService;

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


    @GetMapping("/me/dashboard")
    @Operation(summary = "[사장] 내 대시보드 조회", description = "사용자의 가게 대시보드 데이터를 조회합니다.")
    public ApiResult<DashboardResponse> getMyDashboard(Authentication authentication) {
        Long ownerUserId = Long.parseLong(authentication.getName());
        return ApiResult.success(storeService.getMyDashboard(ownerUserId));
    }


    @GetMapping("/{storeId}/regular")
    @Operation(summary = "[단골] 단골 여부 확인", description = "사용자의 특정 가게 단골 여부를 확인합니다.")
    public ApiResult<Boolean> isRegular(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isRegular = storeService.isRegular(userId, storeId);
        return ApiResult.success(isRegular);
    }


    @PostMapping("/{storeId}/regular")
    @Operation(summary = "[단골] 단골 등록", description = "사용자가 특정 가게의 단골로 등록됩니다.")
    public ApiResult<IdResponse> registerRegular(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Long regularId = storeService.registerRegular(userId, storeId);
        return ApiResult.success(new IdResponse(regularId));
    }


}