package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.response.DashboardResponse;
import goorm._44.entity.Noti;
import goorm._44.entity.StampAction;
import goorm._44.entity.Store;
import goorm._44.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StampRepository stampRepository;
    private final StampLogRepository stampLogRepository;
    private final NotiRepository notiRepository;
    private final NotiReadRepository notiReadRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long ownerUserId) {
        // 1) 사장님 검증
        userRepository.findById(ownerUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2) 사장님 가게 찾기 (한 개 가정)
        Store store = storeRepository.findByUserId(ownerUserId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Long storeId = store.getId();

        // --- 총 방문 수 ---
        int totalVisits = stampRepository.sumTotalStampsByStore(storeId);

//        // --- 오늘/어제 요약 --- [중복 집계]
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

        // --- 오늘/어제 요약 ---
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


        // --- 지역별 단골 비율 ---
        List<Object[]> regionData = stampRepository.countByRegionForStore(storeId);
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

        // --- 최신 공지 반응 ---
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
}