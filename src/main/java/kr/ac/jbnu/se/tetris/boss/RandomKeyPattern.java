package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

public final class RandomKeyPattern implements BossPattern {

    private final RandomKeyMappingGenerator generator;
    private int[] previousPrimary;
    private int[] previousSecondary;

    public RandomKeyPattern(Random random) {
        generator = new RandomKeyMappingGenerator(random);
    }

    @Override
    public int intervalMillis() {
        return 20_000;
    }

    @Override
    public int warningMillis() {
        return 3_000;
    }

    @Override
    public String warningMessage() {
        return "!! 조작키가 뒤섞인다 !!";
    }

    @Override
    public void attack(BossPatternTarget target) {
        int[] primary = generator.generate(target.primaryOriginalKeys(), previousPrimary);
        int[] secondary = null;
        if (target.isTwoPlayerMode()) {
            secondary = generator.generate(target.secondaryOriginalKeys(), previousSecondary);
        }
        previousPrimary = primary.clone();
        previousSecondary = secondary == null ? null : secondary.clone();
        target.applyTemporaryKeys(primary, secondary, 10_000);
        target.showBossMessage("악의 신의 저주! 10초 동안 조작키가 뒤섞인다");
    }
}
