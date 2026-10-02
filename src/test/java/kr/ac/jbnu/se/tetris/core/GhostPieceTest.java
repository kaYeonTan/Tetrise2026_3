package kr.ac.jbnu.se.tetris.core;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.Tetrominoes;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GhostPieceTest {

	@Test
	void ghostYBelowCurrentWhenAirborne() {
		GameSession s = new GameSession(0);
		s.curPiece.setShape(Tetrominoes.LineShape);
		s.curX = 5;
		s.curY = 18;
		assertTrue(s.ghostLandingY() < s.curY);
	}
}
