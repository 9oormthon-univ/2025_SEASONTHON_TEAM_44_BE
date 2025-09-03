package groom._55.controller;

import groom._55.service.PresignService;
import groom._55.dto.PresignDto.PresignRequest;
import groom._55.dto.PresignDto.PresignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Files", description = "S3 Presign 관련 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final PresignService presignService;

    @Operation(
            summary = "PUT 업로드용 Presigned URL 발급",
            description = """
            클라이언트가 S3에 직접 PUT 업로드할 수 있도록 프리사인 URL을 발급합니다.
            응답의 url로 PUT 요청을 보내고, Content-Type 헤더는 요청 시 지정한 값과 동일해야 합니다.
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "프리사인 발급 성공",
            content = @Content(
                    schema = @Schema(implementation = PresignResponse.class),
                    examples = @ExampleObject(name = "성공 예시", value = """
            {
              "key": "uploads/2025/09/uuid_logo.png",
              "url": "https://<bucket>.s3.ap-northeast-2.amazonaws.com/uploads/2025/09/uuid_logo.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
              "expiresAt": 1756889041070
            }
            """)
            )
    )
    @PostMapping("/presign")
    public ResponseEntity<PresignResponse> presign(@Valid @RequestBody PresignRequest req) {
        // (선택) 간단 MIME 화이트리스트
        // if (req.contentType()==null || !req.contentType().matches("^image/(png|jpeg|webp)$"))
        //     throw new IllegalArgumentException("only png/jpeg/webp allowed");
        return ResponseEntity.ok(presignService.presign(req));
    }
}
