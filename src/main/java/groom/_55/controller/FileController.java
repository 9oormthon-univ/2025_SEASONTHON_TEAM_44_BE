package groom._55.controller;

import groom._55.dto.request.PresignRequest;
import groom._55.dto.response.PresignResponse;
import groom._55.service.PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final PresignService presignService;

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
