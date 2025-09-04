package groom._55.config.exception;

import groom._55.config.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<?>> handleCustomException(CustomException e) {
        log.error("❌ 예외 발생: {}", e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())  // ex. 404
                .body(ApiResult.error(e.getErrorCode()));
    }
}
