package groom._55.service;

import groom._55.config.exception.CustomException;
import groom._55.config.exception.ErrorCode;
import groom._55.dto.request.NotiCreateRequest;
import groom._55.dto.response.NotiDetailResponse;
import groom._55.dto.response.NotiLogResponse;
import groom._55.entity.Noti;
import groom._55.entity.NotiTarget;
import groom._55.entity.Store;
import groom._55.entity.User;
import groom._55.repository.NotiReadRepository;
import groom._55.repository.NotiRepository;
import groom._55.repository.StampRepository;
import groom._55.repository.StoreRepository;
import groom._55.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotiService {
    private final NotiRepository notiRepository;
    private final NotiReadRepository notiReadRepository;
    private final StampRepository stampRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    private static final int CERTIFIED_THRESHOLD = 10; // totalStamp 기준

    @Transactional
    public Long createNoti(NotiCreateRequest req, Long userId) {
        // 1) 유저 검증
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2) 유저가 등록한 가게 찾기
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Long storeId = store.getId();

        // 3) 대상자 수 계산
        int targetCount = switch (req.target()) {
            case ALL -> stampRepository.countByStoreId(storeId);
            case CERTIFIED -> stampRepository.countByStoreIdAndTotalStampGreaterThanEqual(storeId, CERTIFIED_THRESHOLD);
            case BASIC -> stampRepository.countByStoreIdAndTotalStampLessThan(storeId, CERTIFIED_THRESHOLD);
        };

        // 4) 저장
        Noti noti = Noti.builder()
                .title(req.title())
                .content(req.content())
                .target(req.target())
                .targetCount(targetCount)
                .store(store) // 실제 Store 엔티티 연결
                .build();

        return notiRepository.save(noti).getId();
    }


    @Transactional(readOnly = true)
    public List<NotiLogResponse> getNotiLogs(Long userId) {
        // 사장님 → 본인 가게 찾기
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 가게의 모든 공지 조회
        List<Noti> notis = notiRepository.findByStoreId(store.getId());

        return notis.stream()
                .map(noti -> new NotiLogResponse(
                        noti.getId(),
                        noti.getTitle(),
                        noti.getTarget().name(),
                        noti.getTargetCount(),
                        noti.getNotiRead().size(), // 열람 수 = 읽은 사용자 수
                        noti.getCreatedAt()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public NotiDetailResponse getUnreadNoti(Long userId, Long notiId) {
        Noti noti = notiRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("공지 없음"));

        // 이미 읽었거나 대상이 아니면 null 반환
        boolean alreadyRead = notiReadRepository.existsByUserIdAndNotiId(userId, notiId);
        if (alreadyRead || !isTargetUser(noti, userId)) {
            return null;
        }

        return new NotiDetailResponse(
                noti.getId(),
                noti.getTitle(),
                noti.getContent(),
                noti.getTarget(),
                noti.getTargetCount(),
                noti.getCreatedAt()
        );
    }

    private boolean isTargetUser(Noti noti, Long userId) {
        if (noti.getTarget() == NotiTarget.ALL) return true;

        int totalStamp = stampRepository.findTotalStampByUserAndStore(userId, noti.getStore().getId());

        return switch (noti.getTarget()) {
            case BASIC -> totalStamp < CERTIFIED_THRESHOLD;
            case CERTIFIED -> totalStamp >= CERTIFIED_THRESHOLD;
            default -> false;
        };
    }
}
