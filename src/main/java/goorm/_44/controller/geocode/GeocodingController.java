package goorm._44.controller.geocode;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.response.GeocodeResponse;
import goorm._44.service.geocode.GeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/geocode")
@Tag(name = "Geocode", description = "위경도 주소 관련 API")
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/address")
    @Operation(summary = "위경도 변환", description = "위도(lat), 경도(lng)를 주소로 변환합니다.")
    public Mono<ApiResult<GeocodeResponse>> getAddress(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return geocodingService.getAddressFromCoordinates(lat, lng)
                .map(opt -> ApiResult.success(new GeocodeResponse(opt.orElse(null))));
    }
}
