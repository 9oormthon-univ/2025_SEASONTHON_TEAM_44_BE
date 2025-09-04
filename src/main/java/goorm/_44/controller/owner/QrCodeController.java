package goorm._44.controller.owner;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import goorm._44.config.api.ApiResult;
import goorm._44.config.exception.CustomException;
import goorm._44.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/owner/qr")
@Tag(name = "Owner-QR", description = "QR 관련 API")
public class QrCodeController {

    @GetMapping
    @Operation(summary = "QR 코드 생성", description = "입력한 url을 QR 코드 이미지로 변환합니다.")
    public ApiResult<QrResponse> generateQr(@RequestParam String url) {
        try {
            int width = 200;
            int height = 200;

            BitMatrix encode = new MultiFormatWriter()
                    .encode(url, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(encode, "PNG", out);

            String base64Image = "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(out.toByteArray());

            return ApiResult.success(new QrResponse(base64Image));
        } catch (WriterException | IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public record QrResponse(String qrImage) {}
}

//    // 1. QR 코드 생성기 페이지를 반환합니다.
//    @GetMapping("/qr_code")
//    public String qrCodePage() {
//        return "qr_generator";
//    }
//
//    // 2. URL을 받아서 QR 코드 이미지를 반환합니다.
//    @GetMapping(value = "/qr/generate")
//    public ResponseEntity<byte[]> generateQrCode(@RequestParam String url) {
//        System.out.println("hello");
//        try {
//            int width = 200;
//            int height = 200;
//
//            BitMatrix encode = new MultiFormatWriter()
//                    .encode(url, BarcodeFormat.QR_CODE, width, height);
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            MatrixToImageWriter.writeToStream(encode, "PNG", out);
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_PNG)
//                    .body(out.toByteArray());
//        } catch (WriterException | IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().build();
//        }
//    }
//}