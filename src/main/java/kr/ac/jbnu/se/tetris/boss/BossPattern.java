package kr.ac.jbnu.se.tetris.boss;

import java.util.Optional;
import kr.ac.jbnu.se.tetris.audio.SfxType;

public interface BossPattern {

    int intervalMillis();

    int warningMillis();

    String warningMessage();

    default Optional<SfxType> activationSound() {
        return Optional.empty();
    }

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
