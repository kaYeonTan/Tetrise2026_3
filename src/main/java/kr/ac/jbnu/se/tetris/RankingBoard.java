package kr.ac.jbnu.se.tetris;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 플레이어별 최고 기록을 뽑아 순위를 매긴다. 화면(GamePanel)과 파일에서 떨어져 있어 단독 테스트가 된다. */
public final class RankingBoard {

    private RankingBoard() {
    }

    /** byStage가 false면 점수 순, true면 스테이지 순(같으면 점수 순). */
    public static List<RecordEntry> rank(List<RecordEntry> records, boolean byStage) {
        Comparator<RecordEntry> order = byStage ? byStageThenScore() : byScore();
        Map<String, RecordEntry> best = new LinkedHashMap<>();
        for (RecordEntry r : records) {
            RecordEntry old = best.get(r.nick());
            if (old == null || order.compare(r, old) < 0) {
                best.put(r.nick(), r);
            }
        }
        List<RecordEntry> ranked = new ArrayList<>(best.values());
        ranked.sort(order);
        return ranked;
    }

    private static Comparator<RecordEntry> byScore() {
        return (a, b) -> Integer.compare(b.score(), a.score());
    }

    private static Comparator<RecordEntry> byStageThenScore() {
        return (a, b) -> {
            int p = Integer.compare(b.progress(), a.progress());
            return p != 0 ? p : Integer.compare(b.score(), a.score());
        };
    }
}
