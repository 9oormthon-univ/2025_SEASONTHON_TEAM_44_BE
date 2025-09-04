package groom._55.service;

import groom._55.dto.request.NotiCreateRequest;
import groom._55.dto.response.NotiDetailResponse;
import groom._55.entity.Noti;
import groom._55.entity.NotiTarget;
import groom._55.entity.Store;
import groom._55.repository.NotiReadRepository;
import groom._55.repository.NotiRepository;
import groom._55.repository.StampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotiService {
    private final NotiRepository notiRepository;
    private final NotiReadRepository notiReadRepository;
    private final StampRepository stampRepository;

    // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
    private static final Long STORE_ID = 1L;
    private static final int CERTIFIED_THRESHOLD = 10; // totalStamp 기준

    @Transactional
    public Long createNoti(NotiCreateRequest req) {
        // 1) 대상자 수 계산
        int targetCount = switch (req.target()) {
            case ALL -> stampRepository.countByStoreId(STORE_ID);
            case CERTIFIED -> stampRepository.countByStoreIdAndTotalStampGreaterThanEqual(STORE_ID, CERTIFIED_THRESHOLD);
            case BASIC -> stampRepository.countByStoreIdAndTotalStampLessThan(STORE_ID, CERTIFIED_THRESHOLD);
        };

        // 2) 저장
        Noti noti = Noti.builder()
                .title(req.title())
                .content(req.content())
                .target(req.target())
                .targetCount(targetCount)
                .store(Store.builder().id(STORE_ID).build())
                .build();

        return notiRepository.save(noti).getId();
    }

    @Transactional(readOnly = true)
    public NotiDetailResponse getUnreadNoti(Long userId, Long notiId) {
        Noti noti = notiRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("공지 없음"));

        // 이미 읽은 경우
        boolean alreadyRead = notiReadRepository.existsByUserIdAndNotiId(userId, notiId);
        if (alreadyRead) {
            throw new IllegalStateException("이미 읽은 공지입니다.");
        }

        // 대상 조건 체크
        if (!isTargetUser(noti, userId)) {
            throw new IllegalStateException("해당 공지의 대상이 아님");
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

        // 유저의 스탬프 확인
        int totalStamp = stampRepository.findTotalStampByUserAndStore(userId, noti.getStore().getId());

        return switch (noti.getTarget()) {
            case BASIC -> totalStamp < 10;
            case CERTIFIED -> totalStamp >= 10;
            default -> false;
        };
    }
}