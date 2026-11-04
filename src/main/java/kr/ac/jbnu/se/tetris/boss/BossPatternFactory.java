package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

/** Creates the boss pattern of a stage so that BossPatternController does not know concrete patterns. */
public final class BossPatternFactory {

    public static final int NO_BLUEPRINT = -1;

    private final Random random;

    public BossPatternFactory(Random random) {
        this.random = random;
    }

    /** Returns the restored blueprint index, or a random one for stage 3; other stages have none. */
    public int resolveBlueprintIndex(int stage, int restoredBlueprintIndex) {
        if (stage != 3) {
            return NO_BLUEPRINT;
        }
        return restoredBlueprintIndex >= 0
                ? restoredBlueprintIndex
                : random.nextInt(Blueprint.stageThreePatternCount());
    }

    public BossPattern create(int stage, int blueprintIndex) {
        return switch (stage) {
            case 2 -> new RisingBlockPattern();
            case 3 -> new BlueprintPattern(Blueprint.stageThree(blueprintIndex));
            case 4 -> new AccelerationPattern();
            case 5 -> new RandomKeyPattern(random);
            default -> NoBossPattern.INSTANCE;
        };
    }
}
