package kr.ac.jbnu.se.tetris.piece;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.GameSession;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SrsRotationTest {

	@Test
	void kickTableHasFiveTestsForT() {
		assertEquals(5, KickTable.offsets(KickGroup.JLSTZ, RotationState.SPAWN, RotationState.R).length);
		assertEquals(5, KickTable.offsets(KickGroup.I, RotationState.SPAWN, RotationState.R).length);
	}

	@Test
	void oPieceKeepsCoords() {
		GameSession s = new GameSession(0);
		s.curPiece.setShape(Tetrominoes.SquareShape);
		s.curX = 5;
		s.curY = 10;
		int[][] before = s.curPiece.copyCoords();
		s.tryRotate(true);
		s.tryRotate(true);
		assertArrayEquals(before[0], s.curPiece.copyCoords()[0]);
		assertArrayEquals(before[1], s.curPiece.copyCoords()[1]);
	}

	@Test
	void failedRotateDoesNotMove() {
		GameSession s = new GameSession(0);
		s.clearBoard();
		s.curPiece.setShape(Tetrominoes.TShape);
		s.curX = 1;
		s.curY = 1;
		// 왼쪽 벽 쪽에 블록 쌓아서 회전 막기
		for (int y = 0; y < GameSession.HEIGHT; y++)
			s.cells[y * GameSession.WIDTH] = Tetrominoes.SquareShape;
		for (int y = 0; y < GameSession.HEIGHT; y++)
			s.cells[y * GameSession.WIDTH + 1] = Tetrominoes.SquareShape;

		int x = s.curX;
		int y = s.curY;
		boolean ok = s.tryRotate(true);
		if (!ok) {
			assertEquals(x, s.curX);
			assertEquals(y, s.curY);
		}
	}
}
