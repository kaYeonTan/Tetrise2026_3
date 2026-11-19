package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

final class BossPatternFactory {

    private final Random random;

    BossPatternFactory(Random random) {
        this.random = random;
    }

    BossPattern create(int stage, int blueprintIndex) {
        if (stage == 2) {
            return new RisingBlockPattern();
        }
        if (stage == 3) {
            return new BlueprintPattern(Blueprint.stageThree(blueprintIndex));
        }
        if (stage == 4) {
            return new AccelerationPattern();
        }
        if (stage == 5) {
            return new RandomKeyPattern(random);
        }
        return NoBossPattern.INSTANCE;
    }
}