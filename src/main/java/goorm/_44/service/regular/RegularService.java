package goorm._44.service.regular;


import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.response.*;
import goorm._44.entity.*;
import goorm._44.repository.*;
import goorm._44.service.file.PresignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RegularService {
    private final NotiReadRepository notiReadRepository;
    private final UserRepository userRepository;
    private final NotiRepository notiRepository;
    private final StampRepository stampRepository;
    private final StampLogRepository stampLogRepository;
    private final StoreRepository storeRepository;
    private final PresignService presignService;

    private String toImageUrl(String imageKey) {
        return (imageKey == null) ? null : presignService.viewUrl(imageKey, null).url();
    }

//    @Transactional
//    public boolean isRegular(Long userId, Long storeId) {
//        return stampRepository.existsByUserIdAndStoreId(userId, storeId);
//    }

//    @Transactional
//    public void registerRegular(Long userId, Long storeId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//        Store store = storeRepository.findById(storeId)
//                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
//
//        // 이미 단골이면 예외
//        boolean exists = stampRepository.existsByUserIdAndStoreId(userId, storeId);
//        if (exists) {
//            throw new CustomException(ErrorCode.ALREADY_REGULAR);
//        }
//
//        // 1. Stamp 생성
//        Stamp stamp = Stamp.builder()
//                .availableStamp(1)
//                .totalStamp(1)
//                .user(user)
//                .store(store)
//                .build();
//        stampRepository.save(stamp);
//
//        // 2. StampLog 생성 (행동: 신규 등록)
//        StampLog log = StampLog.builder()
//                .stamp(stamp)
//                .store(store)
//                .action(StampAction.REGISTER)
//                .build();
//        stampLogRepository.save(log);
//    }


    @Transactional
    public List<RegularMainResponse> getRegularStores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Stamp> stamps = stampRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (stamps.isEmpty()) return List.of();

        // 1) 스탬프 임박순 정렬: (10 - (available % 10)) % 10  → 남은 개수(0~9)
        //    남은 개수가 작을수록 앞, 동률이면 최근 방문일 최신순
        stamps.sort(Comparator
                .comparingInt((Stamp s) -> {
                    int available = s.getAvailableStamp() == null ? 0 : s.getAvailableStamp();
                    return (10 - (available % 10)) % 10; // 0~9 (0=딱 쿠폰 발급 직후 → 가장 안 임박)
                })
                .thenComparing((Stamp s) -> {
                    // 최근 방문일 (없으면 MIN으로)
                    LocalDateTime last = s.getStore().getLog().stream()
                            .filter(log -> log.getStamp().getUser().getId().equals(userId))
                            .map(StampLog::getCreatedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.MIN);
                    return last;
                }, Comparator.reverseOrder())
        );

        // 2) 매핑
        return stamps.stream()
                .map(stamp -> {
                    Store store = stamp.getStore();

                    LocalDateTime lastVisit = store.getLog().stream()
                            .filter(log -> log.getStamp().getUser().getId().equals(userId))
                            .map(StampLog::getCreatedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    int visitCount = (stamp.getTotalStamp() == null ? 0 : stamp.getTotalStamp());
                    String imageUrl = toImageUrl(store.getImageKey());
                    int available = (stamp.getAvailableStamp() == null ? 0 : stamp.getAvailableStamp());


                    boolean hasNewNoti = notiRepository.findByStoreId(store.getId()).stream()
                            .anyMatch(noti -> isTargetUserByTotal(noti, userId)
                                    && !notiReadRepository.existsByUserIdAndNotiId(userId, noti.getId()));

                    return new RegularMainResponse(
                            store.getId(),
                            store.getName(),
                            imageUrl,
                            lastVisit,
                            visitCount,
                            available,
                            hasNewNoti
                    );
                })
                .toList();
    }

    @Transactional
    public StoreDetailResponse getStoreDetail(Long userId, Long storeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

// 내 스탬프 찾기 (없으면 0 반환)
        Stamp stamp = stampRepository.findByUserIdAndStoreId(userId, storeId).orElse(null);
        int availableStamp = (stamp == null ? 0 : stamp.getAvailableStamp());


        // 이미지 URL
        String imageUrl = toImageUrl(store.getImageKey());


        // 최신 공지
        StoreDetailResponse.NotiSimpleResponse latestNoti = notiRepository.findByStoreId(storeId).stream()
                .sorted(Comparator.comparing(Noti::getCreatedAt).reversed())
                .filter(noti -> isTargetUserByAvailable(noti, userId))
                .filter(noti -> !notiReadRepository.existsByUserIdAndNotiId(userId, noti.getId()))
                .findFirst()
                .map(noti -> new StoreDetailResponse.NotiSimpleResponse(
                        noti.getId(),
                        noti.getTitle(),
                        noti.getContent(),
                        noti.getCreatedAt()
                ))
                .orElse(null);

        // ✅ 포맷 적용해서 반환
        return new StoreDetailResponse(
                store.getId(),
                store.getName(),
                store.getIntroduction(),
                formatPhone(store.getPhone()),  // 010-XXXX-XXXX
                store.getAddress(),
                store.getDetailAddress(),
                formatTime(store.getOpen()),    // HH:mm
                formatTime(store.getClose()),   // HH:mm
                imageUrl,
                availableStamp,
                latestNoti
        );
    }

    private String formatPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }

    private String formatTime(Integer time) {
        if (time == null) return null;
        int hour = time / 100;
        int minute = time % 100;
        return String.format("%02d:%02d", hour, minute);
    }


    // availableStamp 기준 (상세 페이지 등)
    private boolean isTargetUserByAvailable(Noti noti, Long userId) {
        if (noti.getTarget() == NotiTarget.ALL) return true;
        int availableStamp = stampRepository.findAvailableStampByUserAndStore(userId, noti.getStore().getId());
        return switch (noti.getTarget()) {
            case BASIC -> availableStamp < 10;
            case CERTIFIED -> availableStamp >= 10;
            default -> false;
        };
    }

    // totalStamp 기준 (전체 조회 등)
    private boolean isTargetUserByTotal(Noti noti, Long userId) {
        if (noti.getTarget() == NotiTarget.ALL) return true;
        int totalStamp = stampRepository.findTotalStampByUserAndStore(userId, noti.getStore().getId());
        return switch (noti.getTarget()) {
            case BASIC -> totalStamp < 10;
            case CERTIFIED -> totalStamp >= 10;
            default -> false;
        };
    }


    public void main(String userId) {
//        1. 유저 정보 가져오기 (유저 PK찾아오기)
        User byUsername = userRepository.findByName(userId).orElseThrow(() -> new RuntimeException("해당 아이디에 맞는 유저를 찾지 못했습니다."));

//        2. 가져온 유저 정보 바탕으로 단골 가게 및 단골 가게의 최근 방문일, 방문 횟수 등을 가져오기
//        최근 방문일, 방문 횟수는 모두 Regular 바탕으로 가져옴
        Long foundUserId = byUsername.getId(); //위에서 null처리 여부 확인하므로 추가 처리x
    }

//    /regular/store/detail/{storeId} [Get]
//    public RegularStoreDetail getDetail(Long userId, Long storeId) {
//        Store storeById = storeRepository.findById(storeId).orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));
//        Noti latestNoti = notiRepository.findFirstByStoreIdOrderByCreatedAtDesc(storeId)
//                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
//        boolean hasNewNoti = !notiReadRepository.existsByUserIdAndNotiId(userId, latestNoti.getId());
//        return RegularStoreDetail.fromEntity(storeById, latestNoti.getTitle(), latestNoti.getContent(), latestNoti.getId(), hasNewNoti);
//    }

//    /regular/store/detail/{storeId} [Post]로 스탬프를 찍을 시

    public void addStamp(Long storeId, Long userId) {
        // 1. Stamp 엔티티 조회
        Stamp stamp = stampRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저와 스토어의 스탬프를 찾을 수 없습니다."));

        // 2. Stamp 엔티티의 useStamp와 totalStamp를 1씩 증가
        stamp.setAvailableStamp(stamp.getAvailableStamp() + 1);
        stamp.setTotalStamp(stamp.getTotalStamp() + 1);
        stampRepository.save(stamp); // 변경된 내용 저장

        // 3. StampLog 엔티티 생성 및 저장
        StampLog stampLog = StampLog.builder()
                .stamp(stamp)
                .store(stamp.getStore()) // Stamp 엔티티를 통해 Store 참조
                .action(StampAction.VISIT)
                .build();
        stampLogRepository.save(stampLog);
    }

    //    regular/noti/read/{stroeId} 공지 읽기. POST
    public void readNoti(Long userId, Long notiId) {
        // 1. User와 Noti 엔티티를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Noti noti = notiRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));

        // 2. NotiRead 엔티티 생성
        NotiRead notiRead = NotiRead.builder()
                .user(user)
                .noti(noti) // 필드명 NotiId 그대로 사용
                .build();

        // 3. NotiRead 엔티티 저장
        notiReadRepository.save(notiRead);
    }


//    public List<RegularMainResponse> getRegularStores(Long userId) {
//        List<Store> stores = storeRepository.findByUserId(userId);
//        List<RegularMainResponse> result = new ArrayList<>();
//
//        for (Store store : stores) {
//            // 스탬프 정보 가져오기
//            Stamp stamp = stampRepository.findByUserIdAndStoreId(userId, store.getId())
//                    .orElse(null);
//
//            var lastVisit = stamp != null ? stamp.getUpdatedAt() : null;
//            var totalVisits = stamp != null ? stamp.getTotalStamp() : 0;
//
//            // 새로운 알림 여부 체크
//            boolean hasNewNoti = false;
//            List<Noti> notis = notiRepository.findByStoreId(store.getId());
//            for (Noti noti : notis) {
//                if (!notiReadRepository.existsByUserIdAndNotiId(userId, noti.getId())) {
//                    hasNewNoti = true;
//                    break;
//                }
//            }
//
//            result.add(
//                    RegularMainResponse.builder()
//                            .storeId(store.getId())
//                            .storeName(store.getName())
//                            .address(store.getAddress())
//                            .phone(store.getPhone())
//                            .lastVisit(lastVisit)
//                            .totalVisits(totalVisits)
//                            .hasNewNoti(hasNewNoti)
//                            .build()
//            );
//        }
//
//        // ✅ lastVisit 기준으로 정렬 (null 은 마지막으로 보냄)
//        result.sort(Comparator.comparing(
//                r -> r.getLastVisit() == null ? LocalDateTime.MAX : r.getLastVisit()
//        ));
//
//        return result;
//    }


    public List<CouponResponse> getCoupons(Long userId) {
        List<Stamp> stamps = stampRepository.findByUserId(userId);
        List<CouponResponse> result = new ArrayList<>();

        for (Stamp stamp : stamps) {
            int availableStamp = (stamp.getAvailableStamp() != null ? stamp.getAvailableStamp() : 0);
            int couponCount = availableStamp / 10;

            String storeImageUrl = toImageUrl(stamp.getStore().getImageKey());

            result.add(
                    CouponResponse.builder()
                            .stampId(stamp.getId()) //스탬프 ID 추가
                            .storeId(stamp.getStore().getId())
                            .storeName(stamp.getStore().getName())
                            .storeImage(storeImageUrl) // ✅ URL로 내려줌 (필드명 유지)
                            .availableStamp(availableStamp)           //스탬프 수 추가
                            .couponCount(couponCount)
                            .build()
            );
        }
        return result;
    }


    @Transactional
    public void useCoupon(Long userId, Long stampId) {
        Stamp stamp = stampRepository.findByIdAndUserId(stampId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰을 찾을 수 없습니다."));

        if (stamp.getAvailableStamp() == null || stamp.getAvailableStamp() < 10) {
            throw new IllegalStateException("스탬프가 부족하여 쿠폰을 사용할 수 없습니다.");
        }

        StampLog log = StampLog.builder()
                .stamp(stamp)
                .store(stamp.getStore())
                .action(StampAction.COUPON)
                .build();

        stamp.setAvailableStamp(stamp.getAvailableStamp() - 10);
        stamp.setTotalStamp(stamp.getTotalStamp() + 1);

        stampLogRepository.save(log);
        stampRepository.save(stamp);
    }

    public MyPageResponse getMyPage(Long userId) {
        // 1) 단골 가게 수
        int storeCount = stampRepository.countByUserId(userId);

        // 2) 보유 스탬프 수 (단순 합계)
        List<Stamp> stamps = stampRepository.findByUserId(userId);
        int totalStamp = stamps.stream()
                .mapToInt(Stamp::getTotalStamp)
                .sum();

        // 3) 보유 쿠폰 수 (가게별로 10 단위로 나눈 후 합산)
        int couponCount = stamps.stream()
                .mapToInt(stamp -> stamp.getAvailableStamp() / 10)
                .sum();

        // 4) 최근 방문 로그 불러오기 (최신순 여러 개, 중복 포함됨)
        List<StampLog> logs = stampLogRepository.findTop20ByStamp_User_IdOrderByCreatedAtDesc(userId);

        // 5) 중복 제거 후 최근 방문한 가게 3개 추출
        List<RecentStoreWithStampDto> recentStores = logs.stream()
                .filter(distinctByKey(log -> log.getStore().getId())) // storeId 기준 중복 제거
                .limit(3)
                .map(log -> {
                    Stamp stamp = stampRepository.findByUserIdAndStoreId(userId, log.getStore().getId())
                            .orElse(null);

                    int availableStamp = (stamp != null) ? stamp.getAvailableStamp() : 0;
                    String storeImageUrl = toImageUrl(log.getStore().getImageKey());

                    return RecentStoreWithStampDto.builder()
                            .storeId(log.getStore().getId())
                            .storeName(log.getStore().getName())
                            .storeImage(storeImageUrl)
                            .availableStamp(availableStamp)
                            .lastVisitDate(log.getCreatedAt()) // ✅ 최근 방문일 추가
                            .build();
                })
                .collect(Collectors.toList());

        return MyPageResponse.builder()
                .storeCount(storeCount)
                .totalStamp(totalStamp)
                .couponCount(couponCount)
                .recentStores(recentStores)
                .build();
    }



    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}