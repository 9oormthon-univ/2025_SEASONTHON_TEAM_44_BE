package goorm._44.dto.response;

import goorm._44.dto.RecentStoreDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class MyPageResponse {
    private int storeCount;
    private int totalStamp;
    private int couponCount;
    private List<RecentStoreDto> recentStores;
}

