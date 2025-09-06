package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.response.PageResponse;
import goorm._44.dto.response.StampLogForOwnerResponse;
import goorm._44.entity.StampAction;
import goorm._44.entity.StampLog;
import goorm._44.entity.Store;
import goorm._44.repository.StampLogRepository;
import goorm._44.repository.StoreRepository;
import goorm._44.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * 사장님 본인 가게의 방문·적립 이력 페이지 조회 (기본 9개)
     * page: 0-based, size 기본 9
     */
    @Transactional(readOnly = true)
    public PageResponse<StampLogForOwnerResponse> getStampLogsForOwner(Long userId, Integer page, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        Long storeId = store.getId();

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 9 : size;

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StampLog> pageLogs = stampLogRepository.findByStore_Id(storeId, pageable);

        List<StampLogForOwnerResponse> content = pageLogs.getContent().stream()
                .map(log -> {
                    Long customerId = log.getStamp().getUser().getId();

                    int cumulative = stampLogRepository
                            .calculateCumulative(customerId, storeId, log.getCreatedAt());

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

        return new PageResponse<>(
                content,
                pageLogs.getNumber(),
                pageLogs.getSize(),
                pageLogs.getTotalElements(),
                pageLogs.getTotalPages(),
                pageLogs.isFirst(),
                pageLogs.isLast()
        );
    }
}
