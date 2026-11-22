package kr.ac.jbnu.se.tetris.boss;

public final class BlueprintPattern implements BossPattern {

    private final Blueprint blueprint;
    private final BlueprintTarget target;
    private boolean completed;

    public BlueprintPattern(Blueprint blueprint, BlueprintTarget target) {
        this.blueprint = blueprint;
        this.target = target;
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
    public void attack() {
    }

    @Override
    public void onBoardChanged() {
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
    public double progress() {
        return blueprint.fillRatio(target::isBlueprintCellOccupied);
    }

    @Override
    public int violationCount() {
        return blueprint.violationCount(target::isBlueprintCellOccupied);
    }

    @Override
    public Blueprint blueprint() {
        return blueprint;
    }
}
