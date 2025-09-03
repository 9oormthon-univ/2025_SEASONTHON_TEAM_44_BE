package groom._55.service;

import groom._55.dto.PresignDto.PresignRequest;
import groom._55.dto.PresignDto.PresignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignService {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}") private String bucket;
    @Value("${aws.s3.presignExpireSec:300}") private long presignExpireSec;

    public PresignResponse presign(PresignRequest req) {
        String safe = (req.fileName() == null ? "file.bin" : req.fileName())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String ext = extractExt(safe);
        LocalDate d = LocalDate.now();
        String key = "uploads/%04d/%02d/%s_%s.%s".formatted(
                d.getYear(), d.getMonthValue(),
                UUID.randomUUID(), stripExt(safe), ext);

        var put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(req.contentType())
                .build();

        PresignedPutObjectRequest pre = presigner.presignPutObject(b -> b
                .putObjectRequest(put)
                .signatureDuration(Duration.ofSeconds(presignExpireSec)));

        return new PresignResponse(key, pre.url().toString(),
                System.currentTimeMillis() + presignExpireSec * 1000);
    }

    private String extractExt(String name) {
        int i = name.lastIndexOf('.');
        String ext = (i > 0) ? name.substring(i + 1) : "bin";
        return ext.equalsIgnoreCase("jpeg") ? "jpg" : ext.toLowerCase();
    }
    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0) ? name.substring(0, i) : name;
    }
}
