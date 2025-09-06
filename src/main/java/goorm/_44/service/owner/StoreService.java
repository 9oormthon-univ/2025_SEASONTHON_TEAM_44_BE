package goorm._44.service.owner;

import goorm._44.config.exception.CustomException;
import goorm._44.dto.request.StoreCreateRequest;
import goorm._44.dto.response.StoreResponse;
import goorm._44.entity.Store;
import goorm._44.entity.User;
import goorm._44.repository.StoreRepository;
import goorm._44.repository.UserRepository;
import goorm._44.config.exception.ErrorCode;

import goorm._44.service.user.PresignService;
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

        // 👉 여기서 포맷 적용
        String formattedPhone = formatPhone(saved.getPhone());
        String openTime = formatTime(saved.getOpen());
        String closeTime = formatTime(saved.getClose());

        return new StoreResponse(
                saved.getId(),
                saved.getName(),
                imageUrl,
                saved.getIntroduction(),
                formattedPhone,       // 포맷 적용된 번호
                saved.getAddress(),
                saved.getDetailAddress(),
                openTime,             // HH:mm
                closeTime             // HH:mm
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

        // 전화번호 포맷 (010-XXXX-XXXX)
        String formattedPhone = formatPhone(store.getPhone());

        // 오픈/클로즈 시간 포맷 (00:00)
        String openTime = formatTime(store.getOpen());
        String closeTime = formatTime(store.getClose());

        return new StoreResponse(
                store.getId(),
                store.getName(),
                imageUrl,
                store.getIntroduction(),
                formattedPhone,
                store.getAddress(),
                store.getDetailAddress(),
                openTime,
                closeTime
        );
    }

    private String formatPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }

    private String formatTime(Integer time) {
        if (time == null) return null;
        // 930 -> 09:30, 0 -> 00:00, 30 -> 00:30
        int hour = time / 100;
        int minute = time % 100;
        return String.format("%02d:%02d", hour, minute);
    }

}
