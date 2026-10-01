package kr.ac.jbnu.se.tetris.boss;

public interface BossPattern {

    int intervalMillis();

    int warningMillis();

    String warningMessage();

    void attack(BossPatternTarget target);

    default void onBoardChanged(BossPatternTarget target) {
    }

    default boolean usesCustomVictory() {
        return false;
    }

    default double progress(BossPatternTarget target) {
        return 0.0;
    }

    default int violationCount(BossPatternTarget target) {
        return 0;
    }

    default Blueprint blueprint() {
        return null;
    }
}
