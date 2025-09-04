package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.response.DashboardResponse;
import goorm._44.entity.Noti;
import goorm._44.entity.Store;
import goorm._44.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // --- QR 스캔 횟수 ---
        int qrCount = stampLogRepository.countByStore_Id(storeId);

        // --- 오늘 요약 ---
        DashboardResponse.TodaySummary todaySummary = new DashboardResponse.TodaySummary(
                stampLogRepository.countTodayVisitors(storeId),
                stampLogRepository.countTodayNewRegulars(storeId),
                stampLogRepository.countTodayRevisitRegulars(storeId)
        );

        // --- 지역별 단골 비율 ---
        List<Object[]> regionData = stampRepository.countByRegionForStore(storeId);
        int total = regionData.stream().mapToInt(r -> ((Long) r[1]).intValue()).sum();

        List<DashboardResponse.RegionRatio> regionRatios = regionData.stream()
                .map(r -> new DashboardResponse.RegionRatio(
                        (String) r[0],
                        (int) Math.round(((Long) r[1] * 100.0) / total)
                ))
                .toList();

        // --- 최신 공지 ---
        Noti latestNoti = notiRepository.findFirstByStoreIdOrderByCreatedAtDesc(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTI_NOT_FOUND));

        int confirmedCount = notiReadRepository.countByNotiId(latestNoti.getId());
        int unconfirmedCount = latestNoti.getTargetCount() - confirmedCount;

        DashboardResponse.NotiResponseSummary notiResponseSummary =
                new DashboardResponse.NotiResponseSummary(
                        latestNoti.getId(),
                        latestNoti.getTitle(),
                        confirmedCount,
                        unconfirmedCount
                );

        return new DashboardResponse(qrCount, todaySummary, regionRatios, notiResponseSummary);
    }
}