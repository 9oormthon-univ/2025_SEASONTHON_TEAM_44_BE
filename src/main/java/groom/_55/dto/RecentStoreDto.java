package groom._55.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RecentStoreDto {
    private Long storeId;
    private String storeName;
    private String storeImage;
}
