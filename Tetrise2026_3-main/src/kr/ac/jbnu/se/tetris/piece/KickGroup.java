package kr.ac.jbnu.se.tetris.piece;

import kr.ac.jbnu.se.tetris.Tetrominoes;

public enum KickGroup {
	JLSTZ,
	I,
	O;

	public static KickGroup from(Tetrominoes shape) {
		if (shape == Tetrominoes.LineShape)
			return I;
		if (shape == Tetrominoes.SquareShape)
			return O;
		return JLSTZ;
	}
}
