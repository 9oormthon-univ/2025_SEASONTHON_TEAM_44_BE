package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.response.StampLogForOwnerResponse;
import goorm._44.entity.StampAction;
import goorm._44.entity.StampLog;
import goorm._44.entity.Store;
import goorm._44.entity.User;
import goorm._44.repository.StampLogRepository;
import goorm._44.repository.StoreRepository;
import goorm._44.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StampService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StampLogRepository stampLogRepository;

    /**
     * 사장님 본인 가게의 방문·적립 이력 조회
     * - REGISTER → 신규 등록 (비고: 신규 단골 등록)
     * - VISIT → 방문 적립 (availableStamp < 10 → 일반 단골, >= 10 → 인증 단골)
     * - COUPON → 쿠폰 사용 (비고: 쿠폰 N번째 사용)
     */
    @Transactional(readOnly = true)
    public List<StampLogForOwnerResponse> getStampLogsForOwner(Long userId) {
        // 1) 사장님 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2) 사장님 가게(단일) 찾기
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        Long storeId = store.getId();

        // 3) 가게 로그 최신순 조회
        List<StampLog> logs = stampLogRepository.findByStore_IdOrderByCreatedAtDesc(storeId);

// 4) 로그별 누적/행동유형 계산
        return logs.stream()
                .map(log -> {
                    Long customerId = log.getStamp().getUser().getId();

                    // ✅ 해당 시점 고객의 누적 스탬프 수 = availableStamp
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
                            // 고객의 쿠폰 사용 횟수 (= COUPON 로그 개수)
                            int couponCount = stampLogRepository
                                    .countByStamp_User_IdAndStore_IdAndAction(customerId, storeId, StampAction.COUPON);
                            note = "쿠폰 " + couponCount + "번째 사용";
                        }
                        default -> action = "기타";
                    }

                    String customerName = log.getStamp().getUser().getName();

                    return new StampLogForOwnerResponse(
                            log.getCreatedAt(),
                            customerName,
                            action,
                            cumulative,
                            note
                    );
                })
                .toList();

    }
}