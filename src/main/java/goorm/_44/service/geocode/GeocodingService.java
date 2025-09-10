package goorm._44.service.geocode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class GeocodingService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    public GeocodingService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl("https://maps.googleapis.com/maps/api/geocode/json")
                .build();
        this.objectMapper = objectMapper;
    }

    public Mono<Optional<String>> getAddressFromCoordinates(double latitude, double longitude) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latlng", latitude + "," + longitude)
                        .queryParam("key", apiKey)
                        .queryParam("language", "ko")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> Optional.ofNullable(parseSpecificAddress(response)));
    }

    private String parseSpecificAddress(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode results = rootNode.path("results");

            if (results.isArray()) {
                for (JsonNode result : results) {
                    JsonNode addressComponents = result.path("address_components");
                    String dong = null;
                    String gu = null;
                    String city = null;


                    // TODO : [geocode] response 결정 - city/gu/dong 반환 범위
                    for (JsonNode component : addressComponents) {
                        JsonNode types = component.path("types");
                        for (JsonNode type : types) {
                            if ("sublocality_level_2".equals(type.asText()) && component.path("long_name").asText().endsWith("동")) {
                                dong = component.path("long_name").asText();
                            } else if ("sublocality_level_1".equals(type.asText())) {
                                gu = component.path("long_name").asText();
                            } else if ("administrative_area_level_1".equals(type.asText())) {
                                city = component.path("long_name").asText();
                            }
                        }
                    }

                    if (city != null && gu != null && dong != null) {
                        return String.format("%s %s %s", city, gu, dong);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JSON 파싱 오류: " + e.getMessage());
        }
        return null;
    }
}
