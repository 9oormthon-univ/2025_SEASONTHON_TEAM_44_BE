package goorm._44.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class MyPageResponse {
    private int storeCount;   // 단골 가게 수
    private int totalStamp;   // 전체 스탬프 합계
    private int couponCount;  // 총 쿠폰 수
    private List<RecentStoreWithStampDto> recentStores; // ✅ 최근 방문 + 스탬프
}
