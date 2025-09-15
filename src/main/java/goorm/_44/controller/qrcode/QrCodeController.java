package goorm._44.controller.qrcode;

import goorm._44.common.api.ApiResult;
import goorm._44.service.qrcode.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qrcode")
@Tag(name = "QR", description = "QR 관련 API")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @GetMapping
    @Operation(summary = "[사장] QR 코드 생성", description = "입력한 url을 QR 코드 이미지로 변환합니다.")
    public ApiResult<QrResponse> generateQr(@RequestParam String url, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String qrImage = qrCodeService.generateQrImage(url, userId);
        return ApiResult.success(new QrResponse(qrImage));
    }

    public record QrResponse(String qrImage) {}
}
