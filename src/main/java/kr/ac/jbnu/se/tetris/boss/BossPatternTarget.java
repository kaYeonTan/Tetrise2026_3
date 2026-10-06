package kr.ac.jbnu.se.tetris.boss;

public interface BossPatternTarget {

    void showBossWarning(String message);

    void clearBossWarning();

    default void onBossPatternActivated() {
    }

    void showBossMessage(String message);

    void raiseBlocks();

    void startAcceleration(int durationMillis, int gravityMultiplier, int nextPieceMultiplier);

    int[] primaryOriginalKeys();

    int[] secondaryOriginalKeys();

    boolean isTwoPlayerMode();

    void applyTemporaryKeys(int[] primaryKeys, int[] secondaryKeys, int durationMillis);

    void resetTemporaryBossEffects();

    boolean isBlueprintCellOccupied(int row, int column);

    default boolean isBlueprintCellOccupiedForPlayer(int player, int row, int column) {
        return isBlueprintCellOccupied(row, column);
    }

    void completeBlueprintPattern();
}
