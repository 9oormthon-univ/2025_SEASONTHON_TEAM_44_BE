package goorm._44.controller.owner;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.StoreCreateRequest;
import goorm._44.dto.response.StoreResponse;
import goorm._44.service.owner.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner/store")
@RequiredArgsConstructor
@Tag(name = "Owner-Store", description = "가게 관련 API")
public class StoreController {
    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "가게 등록", description = "사장님이 가게 정보를 등록합니다.")
    public ApiResult<StoreResponse> createStore(
            @RequestBody StoreCreateRequest req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(storeService.createStore(req, userId));
    }

    @GetMapping("/me")
    @Operation(summary = "내 가게 조회", description = "자신(현재 로그인한 사장님)의 가게 정보를 조회합니다.")
    public ResponseEntity<StoreResponse> getMyStore(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(storeService.getMyStore(userId));
    }
}