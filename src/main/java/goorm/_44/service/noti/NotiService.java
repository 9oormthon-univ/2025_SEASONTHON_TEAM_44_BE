package goorm._44.service.noti;

import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import goorm._44.dto.request.NotiCreateRequest;
import goorm._44.dto.response.PageResponse;
import goorm._44.dto.response.NotiLogResponse;
import goorm._44.entity.*;
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


    /**
     * [사장] 공지 등록
     */
    @Transactional
    public Long createNoti(NotiCreateRequest req, Long userId) {
        // 1. 사장 검증
        // TODO : 사장 검증 로직 필요
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Long storeId = store.getId();

        // 3. 대상자 수 계산
        int targetCount = switch (req.target()) {
            case ALL -> stampRepository.countByStoreId(storeId);
            case CERTIFIED -> stampRepository.countByStoreIdAndTotalStampGreaterThanEqual(storeId, CERTIFIED_THRESHOLD);
            case BASIC -> stampRepository.countByStoreIdAndTotalStampLessThan(storeId, CERTIFIED_THRESHOLD);
        };

        // 4. 저장
        Noti noti = Noti.builder()
                .title(req.title())
                .content(req.content())
                .target(req.target())
                .targetCount(targetCount)
                .store(store)
                .build();

        return notiRepository.save(noti).getId();
    }


    /**
     * [사장] 공지 로그 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<NotiLogResponse> getNotiLogs(Long userId, Integer page, Integer size) {
        // 1. 사장 검증
        // TODO : 사장 검증 로직 필요
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 3. 페이지 기본값 처리 / 최신순 정렬
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 9 : size;
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. 공지 페이지 조회
        Page<Noti> notiPage = notiRepository.findByStoreId(store.getId(), pageable);

        // 5. 공지별 읽은 수 조회 후 Map 변환
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
                        readCountMap.getOrDefault(noti.getId(), 0),
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

    @Transactional
    public Long readNoti(Long userId, Long notiId) {
        // 1. 단골 검증
        // TODO : 사장 검증 로직 필요
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Noti noti = notiRepository.findById(notiId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTI_NOT_FOUND));

        // 2. 공지 읽기
        NotiRead notiRead = NotiRead.builder()
                .user(user)
                .noti(noti)
                .build();

        notiReadRepository.save(notiRead);
        return notiId;
    }
}
