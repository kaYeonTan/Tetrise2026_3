package kr.ac.jbnu.se.tetris.boss;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.audio.SfxType;

public interface BossPatternTarget {

    void showBossWarning(String message);

    void clearBossWarning();

    default void onBossPatternActivated(SfxType sound) {
    }

    void showBossMessage(String message);

    void raiseBlocks();

    default void raiseBlocks(int garbageHole) {
        raiseBlocks();
    }

    default int selectGarbageHole() {
        return 0;
    }

    default Tetrominoes selectForcedPiece() {
        return Tetrominoes.LineShape;
    }

    default void replaceCurrentPiece(Tetrominoes type) {
    }

    void startAcceleration(int durationMillis, int gravityMultiplier, int nextPieceMultiplier);

    int[] primaryOriginalKeys();

    int[] secondaryOriginalKeys();

    boolean isTwoPlayerMode();

    void applyTemporaryKeyMappings(int[] primaryMapping, int[] secondaryMapping, int durationMillis);

    void resetTemporaryBossEffects();

    boolean isBlueprintCellOccupied(int row, int column);

    void completeBlueprintPattern();
}
