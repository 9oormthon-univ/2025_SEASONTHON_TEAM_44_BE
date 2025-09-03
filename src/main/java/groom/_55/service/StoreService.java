package groom._55.service;

import groom._55.dto.request.StoreCreateRequest;
import groom._55.dto.response.StoreResponse;
import groom._55.entity.Store;
import groom._55.entity.User;
import groom._55.repository.StoreRepository;
import groom._55.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PresignService presignService;

    @Transactional
    public StoreResponse createStore(StoreCreateRequest req) {
        // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
        User owner = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("기본 사장님 계정을 찾을 수 없습니다."));

        Store saved = storeRepository.save(Store.from(req, owner));

        String imageUrl = saved.getImageKey() == null
                ? null
                : presignService.viewUrl(saved.getImageKey(), null).url();
        return new StoreResponse(
                saved.getName(), imageUrl, saved.getIntroduction(), saved.getPhone(),
                saved.getAddress(), saved.getDetailAddress(),
                saved.getOpen(), saved.getClose()
        );
    }

    @Transactional(readOnly = true)
    public StoreResponse getMyStore() {
        // TODO: 로그인 붙이면 현재 로그인한 유저 ID로 교체
        Long ownerId = 1L;

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("사장님 계정을 찾을 수 없습니다."));

        List<Store> stores = storeRepository.findByUserId(ownerId);
        if (stores.isEmpty()) {
            throw new RuntimeException("등록된 가게가 없습니다.");
        }

        Store store = stores.get(0); // 한 개만 있다고 가정

        String imageUrl = (store.getImageKey() == null)
                ? null
                : presignService.viewUrl(store.getImageKey(), null).url();

        return new StoreResponse(
                store.getName(),
                imageUrl,
                store.getIntroduction(),
                store.getPhone(),
                store.getAddress(),
                store.getDetailAddress(),
                store.getOpen(),
                store.getClose()
        );
    }

}
