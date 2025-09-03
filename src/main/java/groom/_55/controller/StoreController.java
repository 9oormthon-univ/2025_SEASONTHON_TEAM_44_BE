package groom._55.controller;

import groom._55.dto.request.StoreCreateRequest;
import groom._55.dto.response.StoreResponse;
import groom._55.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
@Tag(name = "Store", description = "가게 관련 API")
public class StoreController {
    private final StoreService storeService;

    // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
    @PostMapping
    @Operation(summary = "가게 등록", description = "사장님이 가게 정보를 등록합니다.")
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreCreateRequest req) {
        return ResponseEntity.ok(storeService.createStore(req));
    }

    // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
    @GetMapping("/me")
    @Operation(summary = "내 가게 조회", description = "현재 로그인한 사장님의 가게 정보를 조회합니다.")
    public ResponseEntity<StoreResponse> getMyStore() {
        return ResponseEntity.ok(storeService.getMyStore());
    }
}