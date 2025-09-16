package goorm._44.service.stamp;

import goorm._44.common.exception.CustomException;
import goorm._44.common.exception.ErrorCode;
import goorm._44.dto.response.*;
import goorm._44.entity.*;
import goorm._44.enums.*;
import goorm._44.repository.*;
import goorm._44.service.file.PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final CouponRepository couponRepository;
    private final MenuImageRepository menuImageRepository;
    private final PresignService presignService;

    /**
     * [사장] 방문 적립 로그 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<StampLogResponse> getStampLogs(
            Long userId, Integer page, Integer size, String customerName, StampAction actionType
    ) {
        // 1. 사장 검증
        User owner = validateOwner(userId);

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        Long storeId = store.getId();

        // 3. 페이지 기본값 처리 / 최신순 정렬
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 9 : size;
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. 조건별 조회
        Page<StampLog> stampLogPage;
        if (customerName != null && !customerName.isBlank() && actionType != null) {
            stampLogPage = stampLogRepository.findByStoreIdAndStampUserNameContainingAndAction(
                    storeId, customerName, actionType, pageable);
        } else if (customerName != null && !customerName.isBlank()) {
            stampLogPage = stampLogRepository.findByStoreIdAndStampUserNameContaining(
                    storeId, customerName, pageable);
        } else if (actionType != null) {
            stampLogPage = stampLogRepository.findByStoreIdAndAction(
                    storeId, actionType, pageable);
        } else {
            stampLogPage = stampLogRepository.findByStoreId(storeId, pageable);
        }


        List<StampLogResponse> content = stampLogPage.getContent().stream()
                .map(log -> {
                    Long customerId = log.getStamp().getUser().getId();
                    int cumulative = stampLogRepository.calculateCumulative(customerId, storeId, log.getCreatedAt());

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
                            int couponCount = stampLogRepository.countByStamp_User_IdAndStore_IdAndAction(
                                    customerId, storeId, StampAction.COUPON
                            );
                            note = "쿠폰 " + couponCount + "번째 사용";
                        }
                        default -> action = "기타";
                    }

                    return new StampLogResponse(
                            log.getCreatedAt(),
                            log.getStamp().getUser().getId(),
                            log.getStamp().getUser().getName(),
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


    /**
     * [사장] 일간 방문·적립 추이 조회
     */
    @Transactional(readOnly = true)
    public VisitTrendResponse getVisitTrends(Long userId) {
        User owner = validateOwner(userId);

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return new VisitTrendResponse(
                getDailyVisitStats(store),
                getWeeklyVisitStats(store)
        );
    }


    /**
     * [사장] 주간 방문·적립 추이 조회
     */
    @Transactional(readOnly = true)
    public List<VisitTrendResponse.TimeSegment> getDailyVisitStats(Long userId) {
        User owner = validateOwner(userId);

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return getDailyVisitStats(store);
    }

    @Transactional(readOnly = true)
    public List<VisitTrendResponse.DailyStat> getWeeklyVisitStats(Long userId) {
        User owner = validateOwner(userId);

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return getWeeklyVisitStats(store);
    }

    /* 내부 헬퍼 */
    private List<VisitTrendResponse.TimeSegment> getDailyVisitStats(Store store) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<VisitTrendResponse.TimeSegment> segments = new ArrayList<>();
        int[][] ranges = {{0, 6}, {6, 12}, {12, 18}, {18, 24}};

        for (int[] range : ranges) {
            int startHour = range[0];
            int endHour = range[1];

            int total = 0;
            int newUsers = 0;
            int revisits = 0;

            if (endHour <= now.getHour()) {
                total = stampLogRepository.countDistinctUsersByStoreAndDateTimeRange(
                        store.getId(), today, startHour, endHour,
                        List.of(StampAction.VISIT, StampAction.REGISTER, StampAction.COUPON)
                );
                newUsers = stampLogRepository.countDistinctUsersByStoreAndDateTimeRange(
                        store.getId(), today, startHour, endHour,
                        List.of(StampAction.REGISTER)
                );
                revisits = stampLogRepository.countDistinctUsersByStoreAndDateTimeRange(
                        store.getId(), today, startHour, endHour,
                        List.of(StampAction.VISIT, StampAction.COUPON)
                );
            }

            segments.add(new VisitTrendResponse.TimeSegment(
                    startHour, endHour, total, newUsers, revisits
            ));
        }

        return segments;
    }

    private List<VisitTrendResponse.DailyStat> getWeeklyVisitStats(Store store) {
        LocalDate today = LocalDate.now();
        List<VisitTrendResponse.DailyStat> stats = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);

            int total = stampLogRepository.countDistinctUsersByStoreAndDateAndActions(
                    store.getId(), date,
                    List.of(StampAction.VISIT, StampAction.REGISTER, StampAction.COUPON)
            );
            int newUsers = stampLogRepository.countDistinctUsersByStoreAndDateAndAction(
                    store.getId(), date, StampAction.REGISTER
            );
            int revisits = stampLogRepository.countDistinctUsersByStoreAndDateAndActions(
                    store.getId(), date,
                    List.of(StampAction.VISIT, StampAction.COUPON)
            );

            stats.add(new VisitTrendResponse.DailyStat(date, total, newUsers, revisits));
        }

        return stats.stream()
                .sorted(Comparator.comparing(VisitTrendResponse.DailyStat::date))
                .toList();
    }


    /**
     * [단골] 단골 가게 메인 조회
     */
    @Transactional(readOnly = true)
    public List<RegularMainResponse> getRegularStores(Long userId, String keyword, SortType sort) {
        User user = validateRegular(userId);

        List<Stamp> stamps = stampRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (stamps.isEmpty()) return List.of();

        if (keyword != null && !keyword.isBlank()) {
            stamps = stamps.stream()
                    .filter(stamp -> stamp.getStore().getName().contains(keyword))
                    .toList();
        }

        Comparator<Stamp> comparator;
        switch (sort) {

            // STAMP
            // 남은 스탬프가 적을수록 앞에 오도록 정렬
            // 남은 스탬프가 같으면, 최근 방문일이 최신인 순으로.
            case STAMP -> {
                comparator = Comparator
                        .comparingInt((Stamp s) -> {
                            int available = s.getAvailableStamp() == null ? 0 : s.getAvailableStamp();
                            return (10 - (available % 10)) % 10;
                        })
                        .thenComparing((Stamp s) -> {
                            LocalDateTime last = s.getStore().getLog().stream()
                                    .filter(log -> log.getStamp().getUser().getId().equals(userId))
                                    .map(StampLog::getCreatedAt)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(LocalDateTime.MIN);
                            return last;
                        }, Comparator.reverseOrder());
            }

            // OLDEST
            // 단골 등록된 시간이 오래된 순서 (옛날부터 단골인 가게 → 최근 등록 가게)
            case OLDEST -> comparator = Comparator.comparing(Stamp::getCreatedAt);

            // NEWEST
            // 단골 등록된 시간이 최신인 순서 (가장 최근에 등록한 단골 가게부터)
            case NEWEST -> comparator = Comparator.comparing(Stamp::getCreatedAt).reversed();

            // LAST_VISIT
            // 마지막으로 방문한 시점이 최신인 가게부터 보여줌
            case LAST_VISIT -> comparator = Comparator.comparing((Stamp s) ->
                    s.getStore().getLog().stream()
                            .filter(log -> log.getStamp().getUser().getId().equals(userId))
                            .map(StampLog::getCreatedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.MIN)
            ).reversed();
            default -> comparator = Comparator.comparing(Stamp::getCreatedAt).reversed();
        }

        stamps = stamps.stream().sorted(comparator).toList();

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
    public RecommendResponse recommendCategoryStore(Long userId) {
        User user = validateRegular(userId);

        // 1. 단골 스탬프 조회
        List<Stamp> stamps = stampRepository.findByUserId(user.getId());
        if (stamps.isEmpty()) {
            return null;
        }

        // 단골 가게 ID 모아두기
        Set<Long> myStoreIds = stamps.stream()
                .map(stamp -> stamp.getStore().getId())
                .collect(Collectors.toSet());

        // 2. 카테고리별 totalStamp 합산
        Map<String, Integer> categoryVisitCount = new HashMap<>();
        for (Stamp stamp : stamps) {
            String category = stamp.getStore().getCategory();
            int total = (stamp.getTotalStamp() != null ? stamp.getTotalStamp() : 0);
            if (category != null) {
                categoryVisitCount.put(category, categoryVisitCount.getOrDefault(category, 0) + total);
            }
        }

        if (categoryVisitCount.isEmpty()) {
            return null;
        }

        // 3. 가장 많이 방문한 카테고리 선택
        int maxVisit = categoryVisitCount.values().stream().max(Integer::compareTo).orElse(0);
        List<String> topCategories = categoryVisitCount.entrySet().stream()
                .filter(e -> e.getValue() == maxVisit)
                .map(Map.Entry::getKey)
                .toList();

        Random random = new Random();
        String chosenCategory = topCategories.get(random.nextInt(topCategories.size()));

        // 4. 추천 매장 선택
        List<Store> candidateStores = storeRepository.findByCategory(chosenCategory).stream()
                .filter(store -> !myStoreIds.contains(store.getId()))
                .toList();

        if (candidateStores.isEmpty()) {
            return null;
        }

        Store store = candidateStores.get(random.nextInt(candidateStores.size()));

        return RecommendResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .imageUrl(toImageUrl(store.getImageKey()))
                .build();
    }


    /**
     * [단골] 단골 가게 상세 조회
     */
    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(Long userId, Long storeId) {
        User user = validateRegular(userId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 내 스탬프 찾기 (없으면 0 반환)
        Stamp stamp = stampRepository.findByUserIdAndStoreId(userId, storeId).orElse(null);
        int availableStamp = (stamp == null ? 0 : stamp.getAvailableStamp());

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

        // 이미지 URL
        String storeImageUrl = toImageUrl(store.getImageKey());

        // 메뉴판 이미지
        List<String> menuImageUrls = menuImageRepository.findByStoreId(storeId).stream()
                .map(menuImage -> toImageUrl(menuImage.getImageKey()))
                .toList();

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
                storeImageUrl,            // 대표 이미지
                menuImageUrls,       // 메뉴판 이미지들
                availableStamp,
                latestNoti
        );
    }


    /**
     * [단골] 마이페이지 조회
     */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {

        User user = validateRegular(userId);

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
    public List<RegularCouponResponse> getCoupons(Long userId, CouponType type) {
        User user = validateRegular(userId);

        List<Stamp> stamps = stampRepository.findByUserId(userId);
        List<RegularCouponResponse> result = new ArrayList<>();

        for (Stamp stamp : stamps) {
            int availableStamp = (stamp.getAvailableStamp() != null ? stamp.getAvailableStamp() : 0);
            int couponCount = availableStamp / 10;

            Coupon coupon = couponRepository.findByStoreId(stamp.getStore().getId())
                    .orElse(Coupon.createDefault(stamp.getStore()));

            switch (type) {
                case OWNED -> {
                    if (couponCount > 0) {
                        result.add(RegularCouponResponse.builder()
                                .stampId(stamp.getId())
                                .storeId(stamp.getStore().getId())
                                .storeName(stamp.getStore().getName())
                                .storeImage(toImageUrl(stamp.getStore().getImageKey()))
                                .availableStamp(availableStamp)
                                .couponCount(couponCount)
                                .couponName(coupon.getName())
                                .couponBenefit(coupon.getBenefit())
                                .build()
                        );
                    }
                }
                case SCHEDULED -> {
                    if ((availableStamp % 10) > 0) {
                        int stampsLeft = 10 - (availableStamp % 10);
                        result.add(RegularCouponResponse.builder()
                                .stampId(stamp.getId())
                                .storeId(stamp.getStore().getId())
                                .storeName(stamp.getStore().getName())
                                .storeImage(toImageUrl(stamp.getStore().getImageKey()))
                                .availableStamp(availableStamp)
                                .couponCount(0)
                                .stampsLeft(stampsLeft)
                                .couponName(coupon.getName())
                                .couponBenefit(coupon.getBenefit())
                                .build()
                        );
                    }
                }
            }
        }
        return result;
    }



    /**
     * [단골] 쿠폰 사용
     */
    @Transactional
    public void useStamp(Long userId, Long stampId) {

        User user = validateRegular(userId);

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

    private User validateOwner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    private User validateRegular(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.REGULAR) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return user;
    }
}
