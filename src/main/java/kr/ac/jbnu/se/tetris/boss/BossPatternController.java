package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

public final class BossPatternController {

    private final Random random;
    private final BossPatternFactory patternFactory;
    private BossPattern pattern = NoBossPattern.INSTANCE;
    private int elapsedMillis;
    private boolean warning;
    private int blueprintIndex = -1;

    public BossPatternController(Random random) {
        this.random = random;
        this.patternFactory = new BossPatternFactory(random);
    }

    public void startStage(int stage, BossPatternTarget target) {
        int selectedBlueprint = stage == 3 ? random.nextInt(Blueprint.stageThreePatternCount()) : -1;
        startStage(stage, target, selectedBlueprint);
    }

    public void startStage(int stage, BossPatternTarget target, int restoredBlueprintIndex) {
        stop(target);
        blueprintIndex = -1;

        if (stage == 3) {
            blueprintIndex = restoredBlueprintIndex >= 0
                    ? restoredBlueprintIndex
                    : random.nextInt(Blueprint.stageThreePatternCount());
        }

        pattern = patternFactory.create(stage, blueprintIndex, target);
        elapsedMillis = 0;
    }

    public void tick(long deltaMillis, BossPatternCallbacks callbacks) {
        int intervalMillis = pattern.intervalMillis();
        if (intervalMillis <= 0) {
            return;
        }
        elapsedMillis += (int) Math.min(deltaMillis, intervalMillis - elapsedMillis);
        int warningStart = intervalMillis - pattern.warningMillis();
        if (!warning && elapsedMillis >= warningStart) {
            warning = true;
            callbacks.showBossWarning(pattern.warningMessage());
        }
        if (elapsedMillis >= intervalMillis) {
            elapsedMillis = 0;
            warning = false;
            callbacks.clearBossWarning();
            pattern.activationSound().ifPresent(callbacks::onBossPatternActivated);
            pattern.attack();
        }
    }

    public void restoreElapsed(int elapsedMillis, BossPatternCallbacks callbacks) {
        if (pattern.intervalMillis() <= 0) {
            this.elapsedMillis = 0;
            return;
        }
        this.elapsedMillis = Math.max(0, elapsedMillis % pattern.intervalMillis());
        warning = this.elapsedMillis >= pattern.intervalMillis() - pattern.warningMillis();
        if (warning) {
            callbacks.showBossWarning(pattern.warningMessage());
        } else {
            callbacks.clearBossWarning();
        }
    }

    public int elapsedMillis() {
        return elapsedMillis;
    }

    public void onBoardChanged() {
        pattern.onBoardChanged();
    }

    public boolean usesCustomVictory() {
        return pattern.usesCustomVictory();
    }

    public double progress() {
        return pattern.progress();
    }

    public int violationCount() {
        return pattern.violationCount();
    }

    public Blueprint blueprint() {
        return pattern.blueprint();
    }

    public int blueprintIndex() {
        return blueprintIndex;
    }

    public void stop(BossPatternCallbacks callbacks) {
        warning = false;
        elapsedMillis = 0;
        callbacks.clearBossWarning();
        callbacks.resetTemporaryBossEffects();
    }
}
