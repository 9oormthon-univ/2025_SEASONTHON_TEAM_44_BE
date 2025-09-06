package goorm._44.controller.user;

import goorm._44.config.api.ApiResult;
import goorm._44.dto.request.PresignRequest;
import goorm._44.dto.response.PresignResponse;
import goorm._44.service.user.PresignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner/file")
@RequiredArgsConstructor
@Tag(name = "User-File", description = "이미지 관련 API")
public class FileController {

    private final PresignService presignService;

    @Operation(
            summary = "이미지 업로드용 Presigned URL 생성",
            description = """
        S3에 이미지를 업로드할 수 있도록 Presigned URL을 생성합니다.
        이후 프론트엔드에서는 응답받은 url로 PUT 요청을 보내 실제 파일을 업로드해야 합니다.
        **request**
        - fileName: 업로드할 파일 이름 (예: logo.png)  
        - contentType: 업로드할 파일의 MIME 타입 (예: image/png)  
        **response**
        - key: S3에 저장될 파일 경로 (DB에 저장할 값)  
        - url: 해당 파일을 업로드할 수 있는 Presigned URL  
        - expiresAt: Presigned URL 만료 시각 (epoch millis)
        """
    )
    @PostMapping("/presign")
    public ApiResult<PresignResponse> presign(@RequestBody PresignRequest req) {
        return ApiResult.success(presignService.presign(req));
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
