package goorm._44.service.coupon;

import goorm._44.common.exception.CustomException;
import goorm._44.common.exception.ErrorCode;
import goorm._44.dto.request.CouponRequest;
import goorm._44.dto.response.CouponResponse;
import goorm._44.dto.response.RegularCouponResponse;
import goorm._44.entity.Coupon;
import goorm._44.entity.Stamp;
import goorm._44.entity.Store;
import goorm._44.entity.User;
import goorm._44.enums.CouponType;
import goorm._44.enums.Role;
import goorm._44.repository.CouponRepository;
import goorm._44.repository.StampRepository;
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
public class CouponService {

    private final CouponRepository couponRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StampRepository stampRepository;
    private final PresignService presignService;

    /**
     * [사장] 내 가게 쿠폰 등록/수정
     */
    @Transactional
    public Long upsertCoupon(Long userId, CouponRequest req) {
        User owner = validateOwner(userId);

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Coupon coupon = couponRepository.findByStoreId(store.getId())
                .orElse(new Coupon(store, null, null, 10));

        coupon.update(req.name(), req.benefit(), 10);

        return couponRepository.save(coupon).getId();
    }


    /**
     * [사장] 내 가게 쿠폰 조회
     */
    @Transactional
    public CouponResponse getMyCoupon(Long userId) {
        User owner = validateOwner(userId);

        Store store = storeRepository.findByUserId(userId).stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Coupon coupon = couponRepository.findByStoreId(store.getId())
                .orElseGet(() -> couponRepository.save(Coupon.createDefault(store)));

        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getBenefit(),
                coupon.getRequiredStamp()
        );
    }

    private User validateOwner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return user;
    }
}
