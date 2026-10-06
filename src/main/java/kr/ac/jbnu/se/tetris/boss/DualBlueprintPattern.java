package kr.ac.jbnu.se.tetris.boss;

/** 2인: P0·P1 각자 도안을 자기 보드에서만 채운다. */
public final class DualBlueprintPattern implements BossPattern {

	private final Blueprint blueprint0;
	private final Blueprint blueprint1;
	private boolean completed0;
	private boolean completed1;

	public DualBlueprintPattern(Blueprint blueprint0, Blueprint blueprint1) {
		this.blueprint0 = blueprint0;
		this.blueprint1 = blueprint1;
	}

	public Blueprint blueprint(int player) {
		return player == 0 ? blueprint0 : blueprint1;
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
		if (!completed0 && blueprint0.isSatisfied((row, column) -> target.isBlueprintCellOccupiedForPlayer(0, row, column))) {
			completed0 = true;
		}
		if (!completed1 && blueprint1.isSatisfied((row, column) -> target.isBlueprintCellOccupiedForPlayer(1, row, column))) {
			completed1 = true;
		}
		if (completed0 && completed1) {
			target.completeBlueprintPattern();
		}
	}

	@Override
	public boolean usesCustomVictory() {
		return true;
	}

	@Override
	public double progress(BossPatternTarget target) {
		double p0 = blueprint0.fillRatio((row, column) -> target.isBlueprintCellOccupiedForPlayer(0, row, column));
		double p1 = blueprint1.fillRatio((row, column) -> target.isBlueprintCellOccupiedForPlayer(1, row, column));
		return (p0 + p1) / 2.0;
	}

	@Override
	public int violationCount(BossPatternTarget target) {
		return blueprint0.violationCount((row, column) -> target.isBlueprintCellOccupiedForPlayer(0, row, column))
				+ blueprint1.violationCount((row, column) -> target.isBlueprintCellOccupiedForPlayer(1, row, column));
	}

	@Override
	public Blueprint blueprint() {
		return blueprint0;
	}
}
