package groom._55.service;

import groom._55.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Value("${aws.s3.publicUrlBase:}")
    private String publicUrlBase; // 퍼블릭 차단이면 이 URL은 바로 접근 안 됨

    public record UpdateImageDto(Long storeId, String imageUrl) {}

    @Transactional
    public UpdateImageDto updateImage(Long storeId, String fileKey) {
        int updated = storeRepository.updateImageKey(storeId, fileKey);
        if (updated == 0) throw new IllegalArgumentException("Store not found: " + storeId);

        // 단순히 표시용 URL 반환(퍼블릭 차단이면 403; 실제 표시는 GET presign을 권장)
        String url = publicUrlBase == null || publicUrlBase.isBlank()
                ? fileKey
                : publicUrlBase + "/" + fileKey;

        return new UpdateImageDto(storeId, url);
    }
}
