package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class RankingBoardTest {

    private static RecordEntry r(String nick, int score, int stage, boolean clear) {
        return new RecordEntry(nick, score, stage, 1, clear, 0L);
    }

    @Test
    void scoreRankKeepsBestPerPlayerAndSortsDescending() {
        List<RecordEntry> ranked = RankingBoard.rank(List.of(
                r("a", 100, 1, false), r("b", 500, 1, false), r("a", 300, 1, false)), false);
        assertEquals(2, ranked.size());
        assertEquals("b", ranked.get(0).nick());
        assertEquals(300, ranked.get(1).score());
    }

    @Test
    void stageRankPrefersFurtherStageThenClearThenScore() {
        List<RecordEntry> ranked = RankingBoard.rank(List.of(
                r("a", 900, 2, false), r("b", 100, 3, false), r("c", 50, 3, true), r("d", 60, 3, true)), true);
        assertEquals("d", ranked.get(0).nick());
        assertEquals("c", ranked.get(1).nick());
        assertEquals("b", ranked.get(2).nick());
        assertEquals("a", ranked.get(3).nick());
    }

    @Test
    void stageRankPicksPlayersBestRunByStage() {
        List<RecordEntry> ranked = RankingBoard.rank(List.of(
                r("a", 9999, 1, false), r("a", 10, 4, false)), true);
        assertEquals(4, ranked.get(0).stage());
    }

    @Test
    void emptyInputGivesEmptyRanking() {
        assertEquals(0, RankingBoard.rank(List.of(), false).size());
    }

    @Test
    void recordLineRoundTripsAndSkipsBrokenLines() {
        RecordEntry e = RecordEntry.fromLine("hero,1200,3,2,1,1700000000000");
        assertEquals("hero,1200,3,2,1,1700000000000", e.toLine());
        assertNull(RecordEntry.fromLine("hero,abc,3,2,1,1"));
        assertNull(RecordEntry.fromLine("hero,1,2"));
    }
}
