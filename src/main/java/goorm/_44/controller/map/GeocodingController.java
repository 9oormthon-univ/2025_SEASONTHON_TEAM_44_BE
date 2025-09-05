package goorm._44.controller.map;

import goorm._44.service.map.GeocodingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/geocode/address")
    public Mono<String> getAddress(@RequestParam double lat, @RequestParam double lng) {
        return geocodingService.getAddressFromCoordinates(lat, lng);
    }
}