package kr.ac.jbnu.se.tetris.boss;

public final class BlueprintPattern implements BossPattern {

    private final Blueprint blueprint;
    private boolean completed;

    public BlueprintPattern(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    @Override
    public int intervalMillis() {
        return 0;
    }

    @Override
    public int warningMillis() {
        return 0;
    }

    @Override
    public String warningMessage() {
        return "";
    }

    @Override
    public void attack(BossPatternTarget target) {
    }

    @Override
    public void onBoardChanged(BossPatternTarget target) {
        if (!completed && blueprint.isSatisfied(target::isBlueprintCellOccupied)) {
            completed = true;
            target.completeBlueprintPattern();
        }
    }

    @Override
    public boolean usesCustomVictory() {
        return true;
    }

    @Override
    public double progress(BossPatternTarget target) {
        return blueprint.fillRatio(target::isBlueprintCellOccupied);
    }

    @Override
    public int violationCount(BossPatternTarget target) {
        return blueprint.violationCount(target::isBlueprintCellOccupied);
    }

    @Override
    public Blueprint blueprint() {
        return blueprint;
    }
}
