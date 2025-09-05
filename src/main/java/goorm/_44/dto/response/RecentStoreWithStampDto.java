package goorm._44.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RecentStoreWithStampDto {
    private Long storeId;
    private String storeName;
    private String storeImage;
    private int availableStamp;
}
