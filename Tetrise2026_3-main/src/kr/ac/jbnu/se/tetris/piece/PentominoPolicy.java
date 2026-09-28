package kr.ac.jbnu.se.tetris.piece;

import java.util.Random;

import kr.ac.jbnu.se.tetris.Tetrominoes;

// F-04 스테이지 3+ 펜토미노 bag 삽입
public class PentominoPolicy {

	public int minStage = 3;
	public double probability = 0.15;

	static final Tetrominoes[] PENTO_POOL = {
			Tetrominoes.P5_P, Tetrominoes.P5_U, Tetrominoes.P5_V, Tetrominoes.P5_X, Tetrominoes.P5_F
	};

	public static PentominoPolicy defaults() {
		return new PentominoPolicy();
	}

	// AC-04-1
	public boolean shouldInsert(int stage, Random r) {
		if (stage < minStage)
			return false;
		return r.nextDouble() < probability;
	}

	public Tetrominoes randomPentomino(Random r) {
		return PENTO_POOL[r.nextInt(PENTO_POOL.length)];
	}
}
