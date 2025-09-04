package goorm._44.controller.owner;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.response.DashboardResponse;
import goorm._44.service.owner.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner/dashboard")
@RequiredArgsConstructor
@Tag(name = "Owner-Dashboard", description = "사장님 대시보드 API")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "대시보드 조회", description = "사장님이 자신의 가게 대시보드 데이터를 조회합니다.")
    public ApiResult<DashboardResponse> getDashboard(Authentication authentication) {
        Long ownerUserId = Long.parseLong(authentication.getName());
        return ApiResult.success(dashboardService.getDashboard(ownerUserId));
    }
}