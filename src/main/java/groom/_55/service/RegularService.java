package groom._55.service;


import groom._55.dto.RegularStoreDetail;
import groom._55.entity.Noti;
import groom._55.entity.Store;
import groom._55.entity.User;
import groom._55.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RegularService {
    private final NotiReadRepository notiReadRepository;
    private final UserRepository userRepository;
    private final NotiRepository notiRepository;
    private final StampRepository stampRepository;
    private final StampLogRepository stampLogRepository;
    private final StoreRepository storeRepository;

    public void main(String userId) {
//        1. 유저 정보 가져오기 (유저 PK찾아오기)
        User byUsername = userRepository.findByName(userId);
        if (byUsername == null) {
            log.info("해당 아이디에 맞는 유저를 찾지 못했습니다.");
        }
//        2. 가져온 유저 정보 바탕으로 단골 가게 및 단골 가게의 최근 방문일, 방문 횟수 등을 가져오기
//        최근 방문일, 방문 횟수는 모두 Regular 바탕으로 가져옴
        Long foundUserId = byUsername.getId(); //위에서 null처리 여부 확인하므로 추가 처리x
    }

    public RegularStoreDetail getDetail(String userId, Long storeId) {
        Store storeById = storeRepository.findById(storeId).orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));
        Noti notiById = notiRepository.findByStoreId(storeId);
        return RegularStoreDetail.fromEntity(storeById, notiById.getTitle(), notiById.getContent());
    }

//    /regular/store/detail/{storeId} [Post]로 스탬프를 찍을 시
    public void enrollRegular(Long storeId) {

    }
}
