package goorm._44.service.store;

import goorm._44.common.exception.CustomException;
import goorm._44.common.exception.ErrorCode;
import goorm._44.entity.Store;
import goorm._44.repository.StampLogRepository;
import goorm._44.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final StampLogRepository stampLogRepository;
    private final StoreRepository storeRepository;

    private static final Map<String, String> INSIGHT_MESSAGES = new HashMap<>();

    static {
        INSIGHT_MESSAGES.put("증가-증가-증가", "오늘은 전반적으로 활발합니다 👏 신규와 재방문이 함께 늘어 충성 고객층 확대에 좋은 날이에요.");
        INSIGHT_MESSAGES.put("증가-증가-0", "방문과 신규 단골은 늘었어요. 재방문도 이어질 수 있도록 혜택을 강조해보세요.");
        INSIGHT_MESSAGES.put("증가-증가-감소", "손님과 신규 단골은 늘었지만, 기존 단골의 재방문은 줄었습니다. 단골 유지 활동이 필요해요.");
        INSIGHT_MESSAGES.put("증가-0-증가", "방문자와 재방문 단골이 늘었습니다. 신규 고객도 단골로 이어질 수 있게 관리해보세요.");
        INSIGHT_MESSAGES.put("증가-0-0", "방문자는 늘었지만 단골 수치는 그대로입니다. 신규 전환을 위한 첫방문 쿠폰 활용이 좋아요.");
        INSIGHT_MESSAGES.put("증가-0-감소", "방문자는 많지만 단골 재방문은 줄었습니다. 재방문 유도를 위한 공지 발송을 고려해보세요.");
        INSIGHT_MESSAGES.put("증가-감소-증가", "방문자와 재방문은 늘었지만 신규 단골은 줄었습니다. 기존 고객층이 두터워지고 있어요.");
        INSIGHT_MESSAGES.put("증가-감소-0", "방문자는 늘었지만 신규 단골은 줄었습니다. 새로운 고객 유입이 단골로 이어질 수 있도록 유도해보세요.");
        INSIGHT_MESSAGES.put("증가-감소-감소", "방문자는 늘었지만 단골 관련 지표는 줄었습니다. 단골 전환에 더 집중할 필요가 있어요.");

        INSIGHT_MESSAGES.put("0-증가-증가", "방문은 비슷하지만 신규와 재방문 모두 늘었습니다 👏 단골 관리가 잘 되고 있어요.");
        INSIGHT_MESSAGES.put("0-증가-0", "신규 단골은 늘었지만 나머지는 유지되었습니다. 장기적으로 재방문도 늘릴 전략이 필요해요.");
        INSIGHT_MESSAGES.put("0-증가-감소", "신규 단골은 늘었지만 재방문은 줄었습니다. 새로운 손님을 단골로 정착시키는 활동이 중요합니다.");
        INSIGHT_MESSAGES.put("0-0-증가", "재방문 단골이 늘었습니다 💙 꾸준히 찾아오는 충성 고객이 늘어나고 있어요.");
        INSIGHT_MESSAGES.put("0-0-0", "어제와 큰 변화는 없습니다. 안정적인 흐름을 유지하고 있습니다.");
        INSIGHT_MESSAGES.put("0-0-감소", "전반적으로 비슷하지만 재방문은 줄었습니다. 기존 단골을 다시 부르는 메시지를 고려해보세요.");
        INSIGHT_MESSAGES.put("0-감소-증가", "신규는 줄었지만 재방문은 늘었습니다. 충성 고객층은 강화되고 있어요.");
        INSIGHT_MESSAGES.put("0-감소-0", "신규 단골이 줄었습니다. 오늘 온 손님이 단골로 이어질 수 있게 쿠폰을 활용해보세요.");
        INSIGHT_MESSAGES.put("0-감소-감소", "신규와 재방문 모두 줄었습니다. 내일은 공지나 혜택으로 분위기를 살려보세요.");

        INSIGHT_MESSAGES.put("감소-증가-증가", "방문은 줄었지만 신규와 재방문 단골은 늘었습니다. 질적인 성과가 좋아지고 있어요 👏");
        INSIGHT_MESSAGES.put("감소-증가-0", "방문은 줄었지만 신규 단골은 늘었습니다. 방문 수 대비 전환율이 높아지고 있어요.");
        INSIGHT_MESSAGES.put("감소-증가-감소", "방문과 재방문은 줄었지만 신규 단골은 늘었습니다. 유입 손님이 단골로 이어지고 있어요.");
        INSIGHT_MESSAGES.put("감소-0-증가", "방문은 줄었지만 재방문은 늘었습니다. 충성 고객의 비중이 커지고 있어요.");
        INSIGHT_MESSAGES.put("감소-0-0", "방문만 줄었고 단골 수치는 유지되었습니다. 내일은 방문자 회복에 집중해보세요.");
        INSIGHT_MESSAGES.put("감소-0-감소", "방문과 재방문이 줄었습니다. 신규 단골 전환 전략으로 활력을 줄 수 있어요.");
        INSIGHT_MESSAGES.put("감소-감소-증가", "방문과 신규 단골은 줄었지만 재방문은 늘었습니다. 충성 고객층이 버팀목이 되고 있어요.");
        INSIGHT_MESSAGES.put("감소-감소-0", "방문과 신규 단골이 줄었습니다. 재방문 고객은 유지되고 있으니 신규 유입에 집중하세요.");
        INSIGHT_MESSAGES.put("감소-감소-감소", "전반적으로 모든 지표가 줄었습니다 😢 내일은 혜택이나 공지로 분위기를 반전시켜보세요.");
    }

    public String getInsight(Long ownerUserId) {

        Long storeId = storeRepository.findByUserId(ownerUserId).getFirst().getId();
        LocalDate targetDate = LocalDate.now();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 오늘 데이터
        int todayVisitors = stampLogRepository.countVisitorsByDate(storeId, targetDate);
        int todayNew = stampLogRepository.countNewRegularsByDate(storeId, targetDate);
        int todayRe = stampLogRepository.countReRegularsByDate(storeId, targetDate);

        // 어제 데이터
        int yesterdayVisitors = stampLogRepository.countVisitorsByDate(storeId, targetDate.minusDays(1));
        int yesterdayNew = store.getYesterdayNewRegular() != null ? store.getYesterdayNewRegular() : 0;
        int yesterdayRe = store.getYesterdayRevisitRegular() != null ? store.getYesterdayRevisitRegular() : 0;

        String visitorTrend = getTrend(yesterdayVisitors, todayVisitors);
        String newTrend = getTrend(yesterdayNew, todayNew);
        String reTrend = getTrend(yesterdayRe, todayRe);

        String key = visitorTrend + "-" + newTrend + "-" + reTrend;
        return INSIGHT_MESSAGES.getOrDefault(key, "어제와 큰 변화는 없습니다. 안정적인 흐름을 유지하고 있습니다.");
    }

    private String getTrend(int yesterday, int today) {
        if (today > yesterday) return "증가";
        if (today < yesterday) return "감소";
        return "0";
    }
}
