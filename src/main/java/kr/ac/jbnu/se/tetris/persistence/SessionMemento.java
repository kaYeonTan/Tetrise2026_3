package kr.ac.jbnu.se.tetris.persistence;

import kr.ac.jbnu.se.tetris.piece.RandomizerState;

/** GameSession + bag 스냅샷 (F-06). */
public class SessionMemento {

	public int playerIndex;
	public int score;
	public int linesRemoved;
	public String[] cells;
	public String activeShape;
	/** SRS 회전 상태 (SPAWN, R, TWO, L). 구 저장 파일은 null일 수 있음. */
	public String activeRotation;
	/** 활성 피스 로컬 좌표 [x0,y0,x1,y1,...] */
	public int[] activeCoords;
	public int curX;
	public int curY;
	public String holdShape;
	public boolean holdLocked;
	public RandomizerState randomizer;
	/** GameEngine.pieceLockSequence — 스냅샷·낙관적 동기화용 */
	public int pieceLockSequence;
}
