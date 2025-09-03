package groom._55.controller;

import groom._55.dto.StoreCreateRequest;
import groom._55.dto.StoreResponse;
import groom._55.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreCreateRequest req) {
        return ResponseEntity.ok(storeService.createStore(req));
    }
}