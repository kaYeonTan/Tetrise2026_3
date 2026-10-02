package kr.ac.jbnu.se.tetris.piece;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.GameSession;

// F-01 SRS 회전 + wall kick
public class SrsRotationSystem {

	public static boolean rotate(GameSession session, boolean clockwise) {
		Shape piece = session.curPiece;
		if (piece.getShape() == Tetrominoes.NoShape)
			return false;

		// O는 돌려도 칸 위치 그대로 (AC-01-3)
		if (piece.getShape() == Tetrominoes.SquareShape)
			return true;

		RotationState from = piece.getRotationState();
		RotationState to = clockwise ? from.cw() : from.ccw();

		Shape rotated = clockwise ? piece.rotateRight() : piece.rotateLeft();
		rotated.setRotationState(to);

		KickGroup group = KickGroup.from(piece.getShape());
		int[][] kicks = KickTable.offsets(group, from, to);

		for (int i = 0; i < kicks.length; i++) {
			int nx = session.curX + kicks[i][0];
			int ny = session.curY - kicks[i][1];
			if (session.tryMove(rotated, nx, ny))
				return true;
		}
		// AC-01-4 5개 다 실패하면 그대로
		return false;
	}
}
