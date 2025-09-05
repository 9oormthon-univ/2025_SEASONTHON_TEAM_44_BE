package goorm._44.dto.response;

import java.util.List;

public record DashboardResponse(
        int totalVisits,             // 지금까지 총 방문 수 (QR + Stamp)
        TodaySummary todaySummary,   // 오늘 요약 (+ 전날 대비 증감)
        List<RegionRatio> regionRatios, // 지역별 단골 비율
        NotiResponseSummary notiResponse // 최신 공지 반응률
) {
    public record TodaySummary(
            int visitors,        // 오늘 방문자 수
            int diffVisitors,    // 전날 대비 증감
            int newRegulars,     // 신규 단골 수
            int diffNewRegulars, // 전날 대비 증감
            int revisitRegulars, // 재방문 단골 수
            int diffRevisitRegulars // 전날 대비 증감
    ) {}

    public record RegionRatio(
            String region,  // 지역명
            int ratio       // 비율(%)
    ) {}

    public record NotiResponseSummary(
            Long notiId,         // 공지 ID
            String title,        // 공지 제목
            Integer confirmedCount, // 확인 처리 완료 수 (없으면 null)
            Integer unconfirmedCount // 미확인 수 (없으면 null)
    ) {}
}
