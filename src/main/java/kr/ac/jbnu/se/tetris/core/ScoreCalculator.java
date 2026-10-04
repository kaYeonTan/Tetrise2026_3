package kr.ac.jbnu.se.tetris.core;

// F-05 점수 (명세: 줄표 × stage, 소프트 1, 하드 2)
public final class ScoreCalculator {

	private static final int[] LINE_TABLE = { 0, 100, 300, 500, 800 };

	private ScoreCalculator() {
	}

	public static int forLines(int lines, int stage) {
		if (lines <= 0 || stage < 1)
			return 0;
		int idx = lines;
		if (idx > 4)
			idx = 4;
		return LINE_TABLE[idx] * stage;
	}

	public static int forSoftDrop(int cells) {
		return cells <= 0 ? 0 : cells;
	}

	public static int forHardDrop(int cells) {
		return cells <= 0 ? 0 : cells * 2;
	}
}
