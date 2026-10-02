package kr.ac.jbnu.se.tetris.persistence;

import kr.ac.jbnu.se.tetris.piece.RandomizerState;

/** GameSession + bag 스냅샷 (F-06). */
public class SessionMemento {

	public int playerIndex;
	public int score;
	public int linesRemoved;
	public String[] cells;
	public String activeShape;
	public int curX;
	public int curY;
	public String holdShape;
	public boolean holdLocked;
	public RandomizerState randomizer;
}
