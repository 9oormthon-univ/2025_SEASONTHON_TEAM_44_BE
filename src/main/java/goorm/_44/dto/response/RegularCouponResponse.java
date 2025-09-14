package goorm._44.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegularCouponResponse {
    private Long stampId;       //스탬프 ID
    private Long storeId;
    private String storeName;
    private String storeImage;
    private int availableStamp; //사용가능 스탬프 수
    private int couponCount;    //쿠폰 수 (availableStamp / 10)
    private Integer stampsLeft;  // 쿠폰 발급까지 남은 스탬프 수
    String couponName;   // 쿠폰 이름
    String couponBenefit; // 쿠폰 혜택
}