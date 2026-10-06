package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

public final class BossPatternController {

    private final Random random;
    private BossPattern pattern = NoBossPattern.INSTANCE;
    private int elapsedMillis;
    private boolean warning;
    private int blueprintIndex = -1;
    private int blueprintIndexP1 = -1;

    public BossPatternController(Random random) {
        this.random = random;
    }

    public void startStage(int stage, BossPatternTarget target) {
        int selectedBlueprint = stage == 3 ? random.nextInt(Blueprint.stageThreePatternCount()) : -1;
        startStage(stage, target, selectedBlueprint);
    }

    public void startStage(int stage, BossPatternTarget target, int restoredBlueprintIndex) {
        startStage(stage, target, restoredBlueprintIndex, -1);
    }

    public void startStage(int stage, BossPatternTarget target, int restoredBlueprintIndex, int restoredBlueprintIndexP1) {
        stop(target);
        blueprintIndex = -1;
        blueprintIndexP1 = -1;
        if (stage == 2) {
            pattern = new RisingBlockPattern();
        } else if (stage == 3) {
            int count = Blueprint.stageThreePatternCount();
            blueprintIndex = restoredBlueprintIndex >= 0
                    ? restoredBlueprintIndex
                    : random.nextInt(count);
            if (target.isTwoPlayerMode()) {
                blueprintIndexP1 = restoredBlueprintIndexP1 >= 0
                        ? restoredBlueprintIndexP1
                        : pickDifferentBlueprintIndex(blueprintIndex, count);
                pattern = new DualBlueprintPattern(
                        Blueprint.stageThree(blueprintIndex),
                        Blueprint.stageThree(blueprintIndexP1));
            } else {
                pattern = new BlueprintPattern(Blueprint.stageThree(blueprintIndex));
            }
        } else if (stage == 4) {
            pattern = new AccelerationPattern();
        } else if (stage == 5) {
            pattern = new RandomKeyPattern(random);
        } else {
            pattern = NoBossPattern.INSTANCE;
        }
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

    public int blueprintIndexP1() {
        return blueprintIndexP1;
    }

    public Blueprint blueprintForPlayer(int player) {
        if (pattern instanceof DualBlueprintPattern dual) {
            return dual.blueprint(player);
        }
        return pattern.blueprint();
    }

    private int pickDifferentBlueprintIndex(int index0, int count) {
        if (count <= 1) {
            return 0;
        }
        int index1 = random.nextInt(count - 1);
        if (index1 >= index0) {
            index1++;
        }
        return index1;
    }

    public void stop(BossPatternTarget target) {
        warning = false;
        elapsedMillis = 0;
        target.clearBossWarning();
        target.resetTemporaryBossEffects();
    }
}
