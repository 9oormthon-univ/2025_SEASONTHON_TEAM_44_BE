package goorm._44.service.store;

import goorm._44.common.exception.CustomException;
import goorm._44.common.exception.ErrorCode;
import goorm._44.dto.response.IdResponse;
import goorm._44.dto.response.MenuImageResponse;
import goorm._44.entity.MenuImage;
import goorm._44.entity.Store;
import goorm._44.entity.User;
import goorm._44.enums.Role;
import goorm._44.repository.MenuImageRepository;
import goorm._44.repository.StoreRepository;
import goorm._44.repository.UserRepository;
import goorm._44.service.file.PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuImageService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MenuImageRepository menuImageRepository;
    private final PresignService presignService;

    @Transactional
    public List<IdResponse> registerMenuImages(Long userId, List<String> keys) {
        // 1. 사장 검증
        User owner = validateOwner(userId);

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 3. 메뉴판 이미지 등록
        List<IdResponse> responses = new ArrayList<>();
        for (String key : keys) {
            MenuImage menuImage = new MenuImage();
            menuImage.setStore(store);
            menuImage.setImageKey(key);

            MenuImage saved = menuImageRepository.save(menuImage);
            responses.add(new IdResponse(saved.getId()));
        }

        return responses;
    }


    @Transactional(readOnly = true)
    public List<MenuImageResponse> getMenuImages(Long userId) {
        // 1. 사장 검증
        User owner = validateOwner(userId);

        // 2. 사장 가게 조회
        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 2. 메뉴판 이미지 조회
        return menuImageRepository.findByStoreId(store.getId()).stream()
                .map(mi -> new MenuImageResponse(
                        mi.getId(),
                        presignService.viewUrl(mi.getImageKey(), null).url()
                ))
                .toList();
    }

//    @Transactional
//    public void deleteMenuImage(Long storeId, Long menuImageId, Long userId) {
//        Store store = validateOwner(userId, storeId);
//
//        MenuImage menuImage = menuImageRepository.findById(menuImageId)
//                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
//        if (!menuImage.getStore().getId().equals(storeId)) {
//            throw new CustomException(ErrorCode.FORBIDDEN);
//        }
//        menuImageRepository.delete(menuImage);
//    }

    private User validateOwner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return user;
    }
}
