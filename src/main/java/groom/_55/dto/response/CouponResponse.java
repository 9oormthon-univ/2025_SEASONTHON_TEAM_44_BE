package groom._55.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long storeId;
    private String storeName;
    private String storeImage;
    private int couponCount; // availableStamp / 10
}