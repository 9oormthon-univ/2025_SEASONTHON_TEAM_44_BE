package goorm._44.service.stamp;

import goorm._44.common.exception.CustomException;
import goorm._44.common.exception.ErrorCode;
import goorm._44.dto.response.*;
import goorm._44.entity.*;
import goorm._44.repository.*;
import goorm._44.service.file.PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class StampService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StampRepository stampRepository;
    private final NotiRepository notiRepository;
    private final NotiReadRepository notiReadRepository;
    private final StampLogRepository stampLogRepository;
    private final PresignService presignService;

    /**
     * [사장] 방문 적립 로그 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<StampLogResponse> getStampLogs(Long userId, Integer page, Integer size) {
        // 1. 사장 검증
        // TODO : 사장 검증 로직 필요
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        Long storeId = store.getId();

        // 3. 페이지 기본값 처리 / 최신순 정렬
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 9 : size;
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. 방문 적립 페이지 조회
        Page<StampLog> stampLogPage = stampLogRepository.findByStore_Id(storeId, pageable);

        List<StampLogResponse> content = stampLogPage.getContent().stream()
                .map(log -> {
                    Long customerId = log.getStamp().getUser().getId();

                    // 누적 스탬프 수 계산
                    int cumulative = stampLogRepository
                            .calculateCumulative(customerId, storeId, log.getCreatedAt());

                    // 액션/노트 변환
                    String action;
                    String note = null;
                    switch (log.getAction()) {
                        case REGISTER -> {
                            action = "신규 등록";
                            note = "신규 단골 등록";
                        }
                        case VISIT -> {
                            action = "방문 적립";
                            note = (cumulative < 10) ? "일반 단골" : "인증 단골";
                        }
                        case COUPON -> {
                            action = "쿠폰 사용";
                            int couponCount = stampLogRepository
                                    .countByStamp_User_IdAndStore_IdAndAction(
                                            customerId, storeId, StampAction.COUPON
                                    );
                            note = "쿠폰 " + couponCount + "번째 사용";
                        }
                        default -> action = "기타";
                    }

                    String customerName = log.getStamp().getUser().getName();

                    return new StampLogResponse(
                            log.getCreatedAt(),
                            customerName,
                            action,
                            cumulative,
                            note
                    );
                })
                .toList();

        return new PageResponse<>(
                content,
                stampLogPage.getNumber(),
                stampLogPage.getSize(),
                stampLogPage.getTotalElements(),
                stampLogPage.getTotalPages(),
                stampLogPage.isFirst(),
                stampLogPage.isLast()
        );
    }


    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

        // 포맷 적용해서 반환
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


    /**
     * [단골] 마이페이지 조회
     */
    @Transactional(readOnly = true)
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
                            .lastVisitDate(log.getCreatedAt())
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


    /**
     * [단골] 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getCoupons(Long userId) {
        List<Stamp> stamps = stampRepository.findByUserId(userId);
        List<CouponResponse> result = new ArrayList<>();

        for (Stamp stamp : stamps) {
            int availableStamp = (stamp.getAvailableStamp() != null ? stamp.getAvailableStamp() : 0);
            int couponCount = availableStamp / 10;

            String storeImageUrl = toImageUrl(stamp.getStore().getImageKey());

            result.add(
                    CouponResponse.builder()
                            .stampId(stamp.getId())
                            .storeId(stamp.getStore().getId())
                            .storeName(stamp.getStore().getName())
                            .storeImage(storeImageUrl)
                            .availableStamp(availableStamp)
                            .couponCount(couponCount)
                            .build()
            );
        }
        return result;
    }


    /**
     * [단골] 쿠폰 사용
     */
    @Transactional
    public void useStamp(Long userId, Long stampId) {
        // 1. Stamp 조회 (내 스탬프인지 검증)
        Stamp stamp = stampRepository.findByIdAndUserId(stampId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.STAMP_NOT_FOUND));

        // 2. 사용 가능 여부 검증
        if (stamp.getAvailableStamp() == null || stamp.getAvailableStamp() < 10) {
            throw new CustomException(ErrorCode.INSUFFICIENT_STAMPS);
        }

        StampLog log = StampLog.builder()
                .stamp(stamp)
                .store(stamp.getStore())
                .action(StampAction.COUPON)
                .build();
        stampLogRepository.save(log);

        stamp.setAvailableStamp(stamp.getAvailableStamp() - 10);
        stamp.setTotalStamp(stamp.getTotalStamp() + 1);
        stampRepository.save(stamp);
    }


    private String toImageUrl(String imageKey) {
        return (imageKey == null) ? null : presignService.viewUrl(imageKey, null).url();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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
}
