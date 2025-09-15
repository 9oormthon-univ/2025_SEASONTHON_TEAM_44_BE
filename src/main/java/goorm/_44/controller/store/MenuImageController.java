package goorm._44.controller.store;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.request.MenuImageRequest;
import goorm._44.dto.response.IdResponse;
import goorm._44.dto.response.MenuImageResponse;
import goorm._44.service.store.MenuImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/me/menu-images")
@RequiredArgsConstructor
@Tag(name = "MenuImage", description = "메뉴판 이미지 관련 API")
public class MenuImageController {
    private final MenuImageService menuImageService;

//    @PostMapping
//    @Operation(summary = "[사장] 메뉴판 이미지 등록", description = "S3에 업로드된 메뉴판 이미지 key를 등록합니다.")
//    public ApiResult<List<IdResponse>> registerMenuImages(
//            @RequestBody MenuImageRequest request,
//            Authentication authentication
//    ) {
//        Long userId = Long.parseLong(authentication.getName());
//        return ApiResult.success(menuImageService.registerMenuImages(userId, request.keys()));
//    }

    @GetMapping
    @Operation(summary = "[사장] 내 메뉴판 이미지 조회", description = "내 가게의 메뉴판 이미지를 조회합니다.")
    public ApiResult<List<MenuImageResponse>> getMenuImages(
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(menuImageService.getMenuImages(userId));
    }

//
//    @DeleteMapping("/{menuImageId}")
//    @Operation(summary = "[사장] 메뉴판 이미지 삭제", description = "특정 메뉴판 이미지를 삭제합니다.")
//    public ApiResult<Void> deleteMenuImage(
//            @PathVariable Long storeId,
//            @PathVariable Long menuImageId,
//            Authentication authentication
//    ) {
//        Long userId = Long.parseLong(authentication.getName());
//        menuImageService.deleteMenuImage(storeId, menuImageId, userId);
//        return ApiResult.success(null);
//    }
}
