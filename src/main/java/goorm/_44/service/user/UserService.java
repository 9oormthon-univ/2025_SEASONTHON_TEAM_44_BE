package goorm._44.service.user;

import goorm._44.common.exception.CustomException;
import goorm._44.common.exception.ErrorCode;
import goorm._44.dto.response.UserSimpleResponse;
import goorm._44.entity.User;
import goorm._44.repository.UserRepository;
import goorm._44.service.file.PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PresignService presignService;

    @Transactional(readOnly = true)
    public boolean isLocationRegistered(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getRegion() != null && !user.getRegion().isBlank();
    }
    @Transactional
    public void updateLocation(Long userId, String region) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setRegion(region);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserSimpleResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new UserSimpleResponse(
                user.getName(),
                user.getProfileImageUrl(),
                user.getRegion()
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

}


