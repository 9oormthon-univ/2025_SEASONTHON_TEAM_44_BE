package groom._55.controller;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller()
public class QrCodeController {

    // 1. QR 코드 생성기 페이지를 반환합니다.
    @GetMapping("/qr_code")
    public String qrCodePage() {
        return "qr_generator";
    }

    // 2. URL을 받아서 QR 코드 이미지를 반환합니다.
    @GetMapping(value = "/qr/generate")
    public ResponseEntity<byte[]> generateQrCode(@RequestParam String url) {
        System.out.println("hello");
        try {
            int width = 200;
            int height = 200;

            BitMatrix encode = new MultiFormatWriter()
                    .encode(url, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(encode, "PNG", out);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(out.toByteArray());
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}