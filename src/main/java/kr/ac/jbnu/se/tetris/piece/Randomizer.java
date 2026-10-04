package kr.ac.jbnu.se.tetris.piece;

import kr.ac.jbnu.se.tetris.Tetrominoes;

// 블록 순서 (7-Bag 등)
public interface Randomizer {
	Tetrominoes next();

	Tetrominoes peekNext();

	default Tetrominoes peekAt(int offset) {
		if (offset == 0)
			return peekNext();
		return Tetrominoes.NoShape;
	}

	long getSeed();

	RandomizerState saveState();

	void restoreState(RandomizerState state);
}
