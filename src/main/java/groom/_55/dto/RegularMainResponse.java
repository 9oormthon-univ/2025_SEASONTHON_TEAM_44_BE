package groom._55.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class RegularMainResponse {
    private Long storeId;
    private String storeName;
    private String category;
    private String address;
    private String phone;

    private LocalDateTime lastVisit; // Stamp updatedAt
    private int totalVisits;         // Stamp totalStamp
    private boolean hasNewNoti;      // 새로운 알림 여부
}