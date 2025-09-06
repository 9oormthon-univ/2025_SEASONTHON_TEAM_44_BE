package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.request.NotiCreateRequest;
import goorm._44.dto.response.PageResponse;
import goorm._44.dto.response.NotiLogResponse;
import goorm._44.entity.Noti;
import goorm._44.entity.NotiTarget;
import goorm._44.entity.Store;
import goorm._44.entity.User;
import goorm._44.repository.NotiReadRepository;
import goorm._44.repository.NotiRepository;
import goorm._44.repository.StampRepository;
import goorm._44.repository.StoreRepository;
import goorm._44.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotiService {
    private final NotiRepository notiRepository;
    private final NotiReadRepository notiReadRepository;
    private final StampRepository stampRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    private static final int CERTIFIED_THRESHOLD = 10;

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
    public PageResponse<NotiLogResponse> getNotiLogs(Long userId, Integer page, Integer size) {
        // 사장님 가게
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 9 : size;

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Noti> notiPage = notiRepository.findByStoreId(store.getId(), pageable);

        // 한 번에 읽음수 맵 생성 (재할당 없음 → effectively final)
        List<Long> ids = notiPage.getContent().stream().map(Noti::getId).toList();
        final Map<Long, Integer> readCountMap = ids.isEmpty()
                ? Collections.emptyMap()
                : notiRepository.countReadsByNotiIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        var content = notiPage.getContent().stream()
                .map(noti -> new NotiLogResponse(
                        noti.getId(),
                        noti.getTitle(),
                        noti.getTarget().name(),
                        noti.getTargetCount(),
                        readCountMap.getOrDefault(noti.getId(), 0), // 열람 수
                        noti.getCreatedAt(),
                        noti.getContent()
                ))
                .toList();

        return new PageResponse<>(
                content,
                notiPage.getNumber(),
                notiPage.getSize(),
                notiPage.getTotalElements(),
                notiPage.getTotalPages(),
                notiPage.isFirst(),
                notiPage.isLast()
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
