package goorm._44.service.user;

import goorm._44.dto.request.PresignRequest;
import goorm._44.dto.response.PresignResponse;
import goorm._44.dto.response.UrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignService {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}") private String bucket;
    @Value("${aws.s3.public:false}") private boolean publicReadable;
    @Value("${aws.s3.publicUrlBase:}") private String publicUrlBase;
    @Value("${aws.s3.presignExpireSec:300}") private long presignExpireSec;

    public PresignResponse presign(PresignRequest req) {
        String safe = (req.fileName() == null ? "file.bin" : req.fileName())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String ext = extractExt(safe);
        String base = stripExt(safe);
        LocalDate d = LocalDate.now();
        String key = "uploads/%04d/%02d/%s_%s.%s".formatted(
                d.getYear(), d.getMonthValue(), UUID.randomUUID(), base, ext);

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

    public UrlResponse presignGet(String key, Long expireSecOverride) {
        long ttl = (expireSecOverride == null || expireSecOverride <= 0)
                ? presignExpireSec : expireSecOverride;

        var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        PresignedGetObjectRequest pre = presigner.presignGetObject(b -> b
                .getObjectRequest(get)
                .signatureDuration(Duration.ofSeconds(ttl)));

        return new UrlResponse(pre.url().toString(),
                System.currentTimeMillis() + ttl * 1000);
    }

    public UrlResponse viewUrl(String key, Long expireSecOverride) {
        if (publicReadable) {
            String base = (publicUrlBase == null || publicUrlBase.isBlank())
                    ? "" : (publicUrlBase.endsWith("/") ? publicUrlBase.substring(0, publicUrlBase.length() - 1) : publicUrlBase);
            String url = base.isEmpty() ? key : base + "/" + key;
            return new UrlResponse(url, -1L); // 퍼블릭은 만료 없음
        }
        return presignGet(key, expireSecOverride);
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
