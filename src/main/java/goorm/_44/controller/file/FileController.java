package goorm._44.controller.file;

import goorm._44.common.api.ApiResult;
import goorm._44.dto.request.PresignBatchRequest;
import goorm._44.dto.request.PresignRequest;
import goorm._44.dto.response.PresignResponse;
import goorm._44.service.file.PresignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "[사장] 이미지 업로드 관련 API")
public class FileController {

    private final PresignService presignService;

    @Operation(summary = "단일 Presigned URL 생성")
    @PostMapping("/presign")
    public ApiResult<PresignResponse> presign(@RequestBody PresignRequest req, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(presignService.presign(req, userId));
    }

    @Operation(summary = "다중 Presigned URL 생성")
    @PostMapping("/presign/batch")
    public ApiResult<List<PresignResponse>> presignBatch(@RequestBody PresignBatchRequest req, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResult.success(
                req.files().stream()
                        .map(r -> presignService.presign(r, userId))
                        .toList()
        );
    }

    // 필요하면 GET용 Presign도 같은 방식으로
    // @GetMapping("/presign-get")
    // public ApiResult<UrlResponse> presignGet(@RequestParam String key,
    //                                          @RequestParam(required = false) Long expiresSec) {
    //     return ApiResult.success(presignService.presignGet(key, expiresSec));
    // }
    //
    // @GetMapping("/view-url")
    // public ApiResult<UrlResponse> viewUrl(@RequestParam String key,
    //                                       @RequestParam(required = false) Long expiresSec) {
    //     return ApiResult.success(presignService.viewUrl(key, expiresSec));
    // }
}
