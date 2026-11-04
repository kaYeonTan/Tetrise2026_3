package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

public final class BossPatternController {

    private final Random random;
    private final BossPatternFactory factory;
    private BossPattern pattern = NoBossPattern.INSTANCE;
    private int elapsedMillis;
    private boolean warning;
    private int blueprintIndex = -1;

    public BossPatternController(Random random) {
        this(random, new BossPatternFactory(random));
    }

    public BossPatternController(Random random, BossPatternFactory factory) {
        this.random = random;
        this.factory = factory;
    }

    public void startStage(int stage, BossPatternTarget target) {
        int selectedBlueprint = stage == 3 ? random.nextInt(Blueprint.stageThreePatternCount()) : -1;
        startStage(stage, target, selectedBlueprint);
    }

    public void startStage(int stage, BossPatternTarget target, int restoredBlueprintIndex) {
        stop(target);
        blueprintIndex = factory.resolveBlueprintIndex(stage, restoredBlueprintIndex);
        pattern = factory.create(stage, blueprintIndex);
        elapsedMillis = 0;
    }

    public void tick(int deltaMillis, BossPatternTarget target) {
        if (pattern.intervalMillis() <= 0) {
            return;
        }
        elapsedMillis += deltaMillis;
        int warningStart = pattern.intervalMillis() - pattern.warningMillis();
        if (!warning && elapsedMillis >= warningStart) {
            warning = true;
            target.showBossWarning(pattern.warningMessage());
        }
        if (elapsedMillis >= pattern.intervalMillis()) {
            elapsedMillis = 0;
            warning = false;
            target.clearBossWarning();
            target.onBossPatternActivated();
            pattern.attack(target);
        }
    }

    public void restoreElapsed(int elapsedMillis, BossPatternTarget target) {
        if (pattern.intervalMillis() <= 0) {
            this.elapsedMillis = 0;
            return;
        }
        this.elapsedMillis = Math.max(0, elapsedMillis % pattern.intervalMillis());
        warning = this.elapsedMillis >= pattern.intervalMillis() - pattern.warningMillis();
        if (warning) {
            target.showBossWarning(pattern.warningMessage());
        } else {
            target.clearBossWarning();
        }
    }

    public int elapsedMillis() {
        return elapsedMillis;
    }

    public void onBoardChanged(BossPatternTarget target) {
        pattern.onBoardChanged(target);
    }

    public boolean usesCustomVictory() {
        return pattern.usesCustomVictory();
    }

    public double progress(BossPatternTarget target) {
        return pattern.progress(target);
    }

    public int violationCount(BossPatternTarget target) {
        return pattern.violationCount(target);
    }

    public Blueprint blueprint() {
        return pattern.blueprint();
    }

    public int blueprintIndex() {
        return blueprintIndex;
    }

    public void stop(BossPatternTarget target) {
        warning = false;
        elapsedMillis = 0;
        target.clearBossWarning();
        target.resetTemporaryBossEffects();
    }
}
