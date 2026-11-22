package kr.ac.jbnu.se.tetris.boss;

import kr.ac.jbnu.se.tetris.audio.SfxType;

public interface BossPatternCallbacks {

    void showBossWarning(String message);

    void clearBossWarning();

    default void onBossPatternActivated(SfxType sound) {
    }

    void resetTemporaryBossEffects();
}
