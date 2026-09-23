package kr.ac.jbnu.se.tetris.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScoreCalculatorTest {

	@Test
	void lineScoreTimesStage() {
		assertEquals(100, ScoreCalculator.forLines(1, 1));
		assertEquals(300, ScoreCalculator.forLines(2, 1));
		assertEquals(600, ScoreCalculator.forLines(2, 2));
		assertEquals(3200, ScoreCalculator.forLines(4, 4));
	}

	@Test
	void dropPoints() {
		assertEquals(3, ScoreCalculator.forSoftDrop(3));
		assertEquals(10, ScoreCalculator.forHardDrop(5));
	}
}
