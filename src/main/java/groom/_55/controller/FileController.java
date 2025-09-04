package groom._55.controller;

import groom._55.dto.request.PresignRequest;
import groom._55.dto.response.PresignResponse;
import groom._55.service.PresignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "File", description = "이미지 관련 API")
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
    public ResponseEntity<PresignResponse> presign(@RequestBody PresignRequest req) {
        return ResponseEntity.ok(presignService.presign(req));
    }

//    @GetMapping("/presign-get")
//    public ResponseEntity<UrlResponse> presignGet(@RequestParam String key,
//                                                  @RequestParam(required = false) Long expiresSec) {
//        return ResponseEntity.ok(presignService.presignGet(key, expiresSec));
//    }
//
//    @GetMapping("/view-url")
//    public ResponseEntity<UrlResponse> viewUrl(@RequestParam String key,
//                                               @RequestParam(required = false) Long expiresSec) {
//        return ResponseEntity.ok(presignService.viewUrl(key, expiresSec));
//    }
}
