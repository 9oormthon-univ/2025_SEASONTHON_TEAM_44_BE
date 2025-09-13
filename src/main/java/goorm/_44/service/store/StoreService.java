package goorm._44.service.store;

import goorm._44.common.exception.CustomException;
import goorm._44.dto.request.StoreCreateRequest;
import goorm._44.dto.response.DashboardResponse;
import goorm._44.dto.response.StoreResponse;
import goorm._44.entity.*;
import goorm._44.enums.Role;
import goorm._44.enums.StampAction;
import goorm._44.repository.*;
import goorm._44.common.exception.ErrorCode;

import goorm._44.service.file.PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StampRepository stampRepository;
    private final StampLogRepository stampLogRepository;
    private final NotiRepository notiRepository;
    private final NotiReadRepository notiReadRepository;
    private final PresignService presignService;

    /**
     * [사장] 가게 등록 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsMyStore(Long userId) {
        User owner = validateOwner(userId);

        return storeRepository.existsByUserId(userId);
    }


    /**
     * [사장] 가게 등록
     */
    @Transactional
    public Long createStore(StoreCreateRequest req, Long userId) {
        User owner = validateOwner(userId);

        boolean exists = storeRepository.existsByUserId(userId);
        if (exists) {
            throw new CustomException(ErrorCode.STORE_ALREADY_EXISTS);
        }

        Store saved = storeRepository.save(Store.from(req, owner));
        return saved.getId();
    }


    /**
     * [사장] 내 가게 조회
     */
    @Transactional(readOnly = true)
    public StoreResponse getMyStore(Long userId) {
        User owner = validateOwner(userId);

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        String imageUrl = (store.getImageKey() == null)
                ? null
                : presignService.viewUrl(store.getImageKey(), null).url();

        // 전화번호 포맷 (010-XXXX-XXXX)
        String formattedPhone = formatPhone(store.getPhone());

        // 오픈/클로즈 시간 포맷 (00:00)
        String openTime = formatTime(store.getOpen());
        String closeTime = formatTime(store.getClose());

        return new StoreResponse(
                store.getId(),
                store.getName(),
                imageUrl,
                store.getIntroduction(),
                formattedPhone,
                store.getAddress(),
                store.getDetailAddress(),
                openTime,
                closeTime
        );
    }


    /**
     * [사장] 내 대시보드 조회
     */
    @Transactional(readOnly = true)
    public DashboardResponse getMyDashboard(Long userId) {
        // 1. 사장 검증
        User owner = validateOwner(userId);

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElse(null);

        if (store == null) {
            return new DashboardResponse(0, null, List.of(), null);
        }

        Long storeId = store.getId();


        // 3a. 총 방문수
        Integer totalVisitsOpt = stampRepository.sumTotalStampsByStore(storeId);
        int totalVisits = (totalVisitsOpt != null) ? totalVisitsOpt : 0;

        // 3b. 오늘/어제 요약 (중복집계)
//        int todayVisitors = stampLogRepository.countByStoreAndDateAndActions(
//                storeId, LocalDate.now(), List.of(StampAction.VISIT, StampAction.REGISTER, StampAction.COUPON));
//        int yesterdayVisitors = stampLogRepository.countByStoreAndDateAndActions(
//                storeId, LocalDate.now().minusDays(1), List.of(StampAction.VISIT, StampAction.REGISTER, StampAction.COUPON));
//
//        int todayNew = stampLogRepository.countByStoreAndDateAndAction(storeId, LocalDate.now(), StampAction.REGISTER);
//        int yesterdayNew = stampLogRepository.countByStoreAndDateAndAction(storeId, LocalDate.now().minusDays(1), StampAction.REGISTER);
//
//        int todayRevisit = stampLogRepository.countByStoreAndDateAndActions(
//                storeId, LocalDate.now(), List.of(StampAction.VISIT, StampAction.COUPON));
//        int yesterdayRevisit = stampLogRepository.countByStoreAndDateAndActions(
//                storeId, LocalDate.now().minusDays(1), List.of(StampAction.VISIT, StampAction.COUPON));
//
//        DashboardResponse.TodaySummary todaySummary = new DashboardResponse.TodaySummary(
//                todayVisitors, todayVisitors - yesterdayVisitors,
//                todayNew, todayNew - yesterdayNew,
//                todayRevisit, todayRevisit - yesterdayRevisit
//        );

        // 3b. 오늘/어제 요약 (중복집계X)
        int todayVisitors = stampLogRepository.countDistinctUsersByStoreAndDateAndActions(
                storeId, LocalDate.now(), List.of(StampAction.VISIT, StampAction.REGISTER, StampAction.COUPON));
        int yesterdayVisitors = stampLogRepository.countDistinctUsersByStoreAndDateAndActions(
                storeId, LocalDate.now().minusDays(1), List.of(StampAction.VISIT, StampAction.REGISTER, StampAction.COUPON));

        int todayNew = stampLogRepository.countDistinctUsersByStoreAndDateAndAction(
                storeId, LocalDate.now(), StampAction.REGISTER);
        int yesterdayNew = stampLogRepository.countDistinctUsersByStoreAndDateAndAction(
                storeId, LocalDate.now().minusDays(1), StampAction.REGISTER);

        int todayRevisit = stampLogRepository.countDistinctUsersByStoreAndDateAndActions(
                storeId, LocalDate.now(), List.of(StampAction.VISIT, StampAction.COUPON));
        int yesterdayRevisit = stampLogRepository.countDistinctUsersByStoreAndDateAndActions(
                storeId, LocalDate.now().minusDays(1), List.of(StampAction.VISIT, StampAction.COUPON));

        DashboardResponse.TodaySummary todaySummary = new DashboardResponse.TodaySummary(
                todayVisitors, todayVisitors - yesterdayVisitors,
                todayNew, todayNew - yesterdayNew,
                todayRevisit, todayRevisit - yesterdayRevisit
        );


        // 3c. 지역별 단골 비율
        List<Object[]> regionData = Optional.ofNullable(stampRepository.countByRegionForStore(storeId))
                .orElse(List.of());

        regionData.sort((a, b) -> Long.compare((Long) b[1], (Long) a[1]));
        int total = regionData.stream().mapToInt(r -> ((Long) r[1]).intValue()).sum();

        List<DashboardResponse.RegionRatio> regionRatios = new ArrayList<>();
        int limit = Math.min(5, regionData.size());
        for (int i = 0; i < limit; i++) {
            Object[] r = regionData.get(i);
            regionRatios.add(new DashboardResponse.RegionRatio(
                    (String) r[0],
                    (int) Math.round(((Long) r[1] * 100.0) / total)
            ));
        }
        if (regionData.size() > 6) {
            int othersCount = regionData.subList(5, regionData.size())
                    .stream().mapToInt(r -> ((Long) r[1]).intValue()).sum();
            regionRatios.add(new DashboardResponse.RegionRatio("기타",
                    (int) Math.round(othersCount * 100.0 / total)));
        }

        // 3d. 최신 공지 반응
        Noti latestNoti = notiRepository.findFirstByStoreIdOrderByCreatedAtDesc(storeId).orElse(null);
        DashboardResponse.NotiResponseSummary notiResponseSummary = null;
        if (latestNoti != null) {
            int confirmedCount = notiReadRepository.countByNotiId(latestNoti.getId());
            int unconfirmedCount = latestNoti.getTargetCount() - confirmedCount;
            notiResponseSummary = new DashboardResponse.NotiResponseSummary(
                    latestNoti.getId(),
                    latestNoti.getTitle(),
                    confirmedCount,
                    unconfirmedCount
            );
        }

        return new DashboardResponse(totalVisits, todaySummary, regionRatios, notiResponseSummary);
    }


    /**
     * [단골] 단골 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasStamp(Long userId, Long storeId) {
        User user = validateRegular(userId);

        return stampRepository.existsByUserIdAndStoreId(userId, storeId);
    }


    /**
     * [단골] 단골 등록
     */
    @Transactional
    public Long registerStamp(Long userId, Long storeId) {
        // 1. 단골 검증
        User user = validateRegular(userId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 2. 이미 단골이면 예외
        boolean exists = stampRepository.existsByUserIdAndStoreId(userId, storeId);
        if (exists) {
            throw new CustomException(ErrorCode.ALREADY_REGULAR);
        }

        Stamp stamp = Stamp.builder()
                .availableStamp(1)
                .totalStamp(1)
                .user(user)
                .store(store)
                .build();
        Stamp saved = stampRepository.save(stamp);

        StampLog log = StampLog.builder()
                .stamp(saved)
                .store(store)
                .action(StampAction.REGISTER)
                .build();
        stampLogRepository.save(log);

        return saved.getId();
    }


    /**
     * [단골] 스탬프 찍기
     */
    @Transactional
    public Long addStamp(Long userId, Long storeId) {
        // 1. 단골 검증
        User user = validateRegular(userId);

        Stamp stamp = stampRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STAMP_NOT_FOUND));

        // 2. Stamp 엔티티의 availableStamp와 totalStamp 증가
        stamp.setAvailableStamp(stamp.getAvailableStamp() + 1);
        stamp.setTotalStamp(stamp.getTotalStamp() + 1);
        stampRepository.save(stamp);

        StampLog stampLog = StampLog.builder()
                .stamp(stamp)
                .store(stamp.getStore())
                .action(StampAction.VISIT)
                .build();
        stampLogRepository.save(stampLog);

        return stamp.getId();
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
