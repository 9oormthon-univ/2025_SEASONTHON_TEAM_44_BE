package goorm._44.controller.map;

import goorm._44.config.api.ApiResult;
import goorm._44.service.map.GeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/geocode")
@Tag(name = "Geocoding", description = "좌표 ↔ 주소 변환 API")
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/address")
    @Operation(summary = "좌표로 주소 조회", description = "위도(lat), 경도(lng)로 도로명/지번 주소를 반환합니다.")
    public Mono<ApiResult<GeocodeResponse>> getAddress(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return geocodingService.getAddressFromCoordinates(lat, lng)
                .map(addr -> ApiResult.success(new GeocodeResponse(addr)));
    }

    // 응답 바디 DTO
    public record GeocodeResponse(String address) {}
}
