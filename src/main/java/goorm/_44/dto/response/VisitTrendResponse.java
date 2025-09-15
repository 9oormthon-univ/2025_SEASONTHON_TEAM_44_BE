package goorm._44.dto.response;

import java.time.LocalDate;
import java.util.List;

public record VisitTrendResponse(
        List<TimeSegment> daily,
        List<DailyStat> weekly
) {
    public static record TimeSegment(
            int startHour,
            int endHour,
            int totalVisitors,
            int newVisitors,
            int revisits
    ) {}

    public static record DailyStat(
            LocalDate date,
            int totalVisitors,
            int newVisitors,
            int revisits
    ) {}
}
