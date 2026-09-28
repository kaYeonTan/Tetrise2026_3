package kr.ac.jbnu.se.tetris.piece;

import kr.ac.jbnu.se.tetris.Tetrominoes;

// 세이브할 때 bag 상태 (S7)
public class RandomizerState {
	public long seed;
	public int index;
	public Tetrominoes[] bagOrder;

	public RandomizerState(long seed, int index, Tetrominoes[] bagOrder) {
		this.seed = seed;
		this.index = index;
		this.bagOrder = bagOrder;
	}
}
