package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.response.StampLogForOwnerResponse;
import goorm._44.entity.StampLog;
import goorm._44.entity.Store;
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
     * - 누적 1회면 "신규 등록", 그 외 "방문 적립"
     * - 비고는 신규 등록일 때 "신규 단골 등록"
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

                    int cumulative = stampLogRepository
                            .countByStamp_User_IdAndStore_IdAndCreatedAtLessThanEqual(
                                    customerId, storeId, log.getCreatedAt()
                            );

                    String action = (cumulative == 1) ? "신규 등록" : "방문 적립";
                    String note = (cumulative == 1) ? "신규 단골 등록" : null;
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