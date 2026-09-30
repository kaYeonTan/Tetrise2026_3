package kr.ac.jbnu.se.tetris.boss;

import java.util.Random;

public final class BossPatternController {

    private final Random random;
    private BossPattern pattern = NoBossPattern.INSTANCE;
    private int elapsedMillis;
    private boolean warning;

    public BossPatternController(Random random) {
        this.random = random;
    }

    public void startStage(int stage, BossPatternTarget target) {
        stop(target);
        if (stage == 2) {
            pattern = new RisingBlockPattern();
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

    public void stop(BossPatternTarget target) {
        warning = false;
        elapsedMillis = 0;
        target.clearBossWarning();
        target.resetTemporaryBossEffects();
    }
}
