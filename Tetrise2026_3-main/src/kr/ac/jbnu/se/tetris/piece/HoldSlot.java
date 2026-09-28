package kr.ac.jbnu.se.tetris.piece;

import kr.ac.jbnu.se.tetris.Tetrominoes;

// F-03 홀드 칸
public class HoldSlot {

	Tetrominoes stored = Tetrominoes.NoShape;

	public Tetrominoes getStored() {
		return stored;
	}

	public void setStored(Tetrominoes shape) {
		stored = shape;
	}

	public void clear() {
		stored = Tetrominoes.NoShape;
	}

	public boolean isEmpty() {
		return stored == Tetrominoes.NoShape;
	}
}
