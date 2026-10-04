package kr.ac.jbnu.se.tetris.core;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.Tetrominoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineClearAnimationTest {

	@Test
	void linesStayUntilCommit() {
		GameSession s = new GameSession(0);
		for (int j = 0; j < GameSession.WIDTH; j++)
			s.cells[j] = Tetrominoes.TShape;

		int pending = s.prepareLineClearAnimation();
		assertEquals(1, pending);
		assertTrue(s.lineClearBlinkRows[0]);
		assertEquals(Tetrominoes.TShape, s.shapeAt(0, 0));

		int cleared = s.commitLineClear();
		assertEquals(1, cleared);
		assertEquals(Tetrominoes.NoShape, s.shapeAt(0, 0));
	}

	@Test
	void twoFullLinesMarkedTogether() {
		GameSession s = new GameSession(0);
		for (int row = 0; row < 2; row++) {
			for (int j = 0; j < GameSession.WIDTH; j++)
				s.cells[(row * GameSession.WIDTH) + j] = Tetrominoes.SquareShape;
		}
		assertEquals(2, s.prepareLineClearAnimation());
		assertEquals(2, s.commitLineClear());
	}
}
