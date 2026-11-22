package kr.ac.jbnu.se.tetris.boss;

import java.util.Optional;
import java.util.Random;
import kr.ac.jbnu.se.tetris.audio.SfxType;

public final class RandomKeyPattern implements BossPattern {

    private final RandomKeyMappingGenerator generator;
    private final RandomKeyTarget target;
    private int[] previousPrimaryMapping;
    private int[] previousSecondaryMapping;

    public RandomKeyPattern(Random random, RandomKeyTarget target) {
        generator = new RandomKeyMappingGenerator(random);
        this.target = target;
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
    public Optional<SfxType> activationSound() {
        return Optional.of(SfxType.STAGE_5_PATTERN);
    }

    @Override
    public void attack() {
        int[] primaryMapping = generator.generatePermutation(
                target.primaryOriginalKeys().length, previousPrimaryMapping);
        int[] secondaryMapping = null;
        if (target.isTwoPlayerMode()) {
            secondaryMapping = generator.generatePermutation(
                    target.secondaryOriginalKeys().length, previousSecondaryMapping);
        }
        previousPrimaryMapping = primaryMapping.clone();
        previousSecondaryMapping = secondaryMapping == null ? null : secondaryMapping.clone();
        target.applyTemporaryKeyMappings(primaryMapping, secondaryMapping, 10_000);
        target.showBossMessage("악의 신의 저주! 10초 동안 조작키가 뒤섞인다");
    }
}
