package goorm._44.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecommendResponse {
    private Long storeId;
    private String name;
    private String address;
    private String imageUrl;
}
