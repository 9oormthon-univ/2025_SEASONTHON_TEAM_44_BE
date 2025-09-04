package goorm._44.dto.response;

import java.util.List;

public record DashboardResponse(
        int qrCount,                // QR 스캔 횟수
        TodaySummary todaySummary,  // 오늘 요약
        List<RegionRatio> regionRatios, // 지역별 단골 비율
        NotiResponseSummary notiResponse // 공지 반응률
) {
    public record TodaySummary(
            int visitors,       // 오늘 방문자 수
            int newRegulars,    // 신규 단골 수
            int revisitRegulars // 재방문 단골 수
    ) {}

    public record RegionRatio(
            String region,  // 지역명
            int ratio       // 비율(%)
    ) {}

    public record NotiResponseSummary(
            Long notiId,        // 공지 ID
            String title,       // 공지 제목
            int confirmedCount, // 확인 처리 완료 수
            int unconfirmedCount // 미확인 수
    ) {}
}
