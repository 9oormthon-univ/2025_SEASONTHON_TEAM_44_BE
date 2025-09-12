package goorm._44.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD REQUEST
    ALREADY_REGULAR(HttpStatus.BAD_REQUEST, "이미 단골로 등록된 사용자입니다."),
    INSUFFICIENT_STAMPS(HttpStatus.BAD_REQUEST, "스탬프가 부족하여 쿠폰을 사용할 수 없습니다."),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 404 NOT FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 가게가 없습니다."),
    NOTI_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 공지가 없습니다."),
    STAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 스탬프가 없습니다."),

    // 409 CONFLICT
    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 가게가 있습니다."),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
