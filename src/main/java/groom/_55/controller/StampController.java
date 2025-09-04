package groom._55.controller;

import groom._55.config.api.ApiResult;
import groom._55.dto.response.StampLogForOwnerResponse;
import groom._55.service.StampService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stamp")
@RequiredArgsConstructor
@Tag(name = "Owner-Visits", description = "방문·적립 관련 API")
public class StampController {

    private final StampService stampService;

    @GetMapping("/owner/logs")
    @Operation(summary = "방문·적립 조회", description = "자신(현재 로그인한 사장님)의 가게 방문·적립 이력을 조회합니다.")
    public ApiResult<List<StampLogForOwnerResponse>> getStampLogsForOwner(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(stampService.getStampLogsForOwner(userId));
    }
}
