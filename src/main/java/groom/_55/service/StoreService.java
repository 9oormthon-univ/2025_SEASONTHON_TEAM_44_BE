package groom._55.service;

import groom._55.dto.StoreCreateRequest;
import groom._55.dto.StoreResponse;
import groom._55.entity.Store;
import groom._55.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final PresignService presignService;

    @Transactional
    public StoreResponse createStore(StoreCreateRequest req) {
        Store saved = storeRepository.save(Store.from(req));
        // 항상 프론트가 바로 쓸 수 있는 URL을 내려준다(퍼블릭/비공개 자동 처리)
        String imageUrl = presignService.viewUrl(saved.getImageKey(), null).url();
        return new StoreResponse(
                saved.getName(), imageUrl, saved.getIntroduction(), saved.getPhone(),
                saved.getAddress(), saved.getDetailAddress(), saved.getCategory(),
                saved.getOpen(), saved.getClose()
        );
    }
}
