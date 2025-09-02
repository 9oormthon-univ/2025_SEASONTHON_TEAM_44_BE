package groom._55.dto;

import groom._55.entity.Store;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegularStoreDetail {
    private Long id;
    private String name;
    private String phone;
    private String category;
    private String address;
    private String detailAddress;
    private String introduction; // 소개 (주어진 엔티티에는 없지만, DTO에 추가하여 사용 가능)
    private String recentNotiTitle; // 최근 공지사항 문자열 필드
    private String recentNotiContent;

    public static RegularStoreDetail fromEntity(Store store, String notificationTitle, String notificationContent) {
        return RegularStoreDetail.builder()
                .id(store.getId())
                .name(store.getName())
                .phone(store.getPhone())
                .category(store.getCategory())
                .address(store.getAddress())
                .detailAddress(store.getDetailAddress())
                // description과 recentNoti는 임시로 고정된 값을 사용합니다.
                // 실제로는 다른 로직을 통해 데이터를 가져와야 합니다.
                .introduction(store.getIntroduction())
                .recentNotiTitle(notificationTitle)
                .recentNotiContent(notificationContent)
                .build();
    }
}