package goorm._44.dto.response;

import goorm._44.entity.Store;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegularStoreDetail {
    private Long notiId;
    private String name;
    private String phone;
    private String address;
    private String detailAddress;
    private String introduction; // 소개 (주어진 엔티티에는 없지만, DTO에 추가하여 사용 가능)
    private String recentNotiTitle; // 최근 공지사항 문자열 필드
    private String recentNotiContent;
    private Integer open;
    private Integer close;
    private boolean hasNewNoti;

    public static RegularStoreDetail fromEntity(Store store, String notificationTitle, String notificationContent, Long notiId, boolean hasNewNoti) {
        return RegularStoreDetail.builder()
                .notiId(notiId)
                .name(store.getName())
                .phone(store.getPhone())
                .address(store.getAddress())
                .detailAddress(store.getDetailAddress())
                .open(store.getOpen())
                .close(store.getClose())
                .introduction(store.getIntroduction())
                .recentNotiTitle(notificationTitle)
                .recentNotiContent(notificationContent)
                .hasNewNoti(hasNewNoti)
                .build();
    }
}