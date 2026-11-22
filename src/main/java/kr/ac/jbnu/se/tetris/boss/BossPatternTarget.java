package kr.ac.jbnu.se.tetris.boss;

/** Capabilities supplied together when the controller starts a stage. */
public interface BossPatternTarget extends BossPatternCallbacks, RisingBlockTarget,
        RandomKeyTarget, AccelerationTarget, BlueprintTarget {

    void raiseBlocks();

    @Override
    default void raiseBlocks(int garbageHole) {
        raiseBlocks();
    }

    @Override
    default int selectGarbageHole() {
        return 0;
    }
}
