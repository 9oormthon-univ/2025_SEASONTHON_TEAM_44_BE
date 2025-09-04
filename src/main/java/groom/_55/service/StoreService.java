package groom._55.service;

import groom._55.config.exception.CustomException;
import groom._55.dto.request.StoreCreateRequest;
import groom._55.dto.response.StoreResponse;
import groom._55.entity.Store;
import groom._55.entity.User;
import groom._55.repository.StoreRepository;
import groom._55.repository.UserRepository;
import groom._55.config.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PresignService presignService;

    @Transactional
    public StoreResponse createStore(StoreCreateRequest req, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        boolean exists = storeRepository.existsByUserId(userId);
        if (exists) {
            throw new CustomException(ErrorCode.STORE_ALREADY_EXISTS);
        }

        Store saved = storeRepository.save(Store.from(req, owner));

        String imageUrl = saved.getImageKey() == null
                ? null
                : presignService.viewUrl(saved.getImageKey(), null).url();

        return new StoreResponse(
                saved.getId(),  saved.getName(), imageUrl, saved.getIntroduction(),
                saved.getPhone(), saved.getAddress(), saved.getDetailAddress(),
                saved.getOpen(), saved.getClose()
        );
    }


    @Transactional(readOnly = true)
    public StoreResponse getMyStore(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        String imageUrl = (store.getImageKey() == null)
                ? null
                : presignService.viewUrl(store.getImageKey(), null).url();

        return new StoreResponse(
                store.getId(),
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
