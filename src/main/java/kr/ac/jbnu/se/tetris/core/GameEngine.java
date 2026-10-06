package kr.ac.jbnu.se.tetris.core;

import java.util.ArrayList;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.event.GameEvent;
import kr.ac.jbnu.se.tetris.event.GameEventBus;
import kr.ac.jbnu.se.tetris.event.GameEventType;
import kr.ac.jbnu.se.tetris.piece.PentominoPolicy;
import kr.ac.jbnu.se.tetris.piece.SevenBagRandomizer;

// 1인/2인 공통 게임 진행 (Timer는 Board에서 tick 호출)
public class GameEngine {

	public static final int LINE_CLEAR_ANIM_MS = 300;
	public static final int MAX_STAGE = 5;

	GameMode mode;
	GameEventBus bus;
	ArrayList<GameSession> sessions;

	boolean isStarted = false;
	boolean isPaused = false;
	boolean isFallingFinished = false;
	boolean gameOverSent = false;
	long bagSeed = System.currentTimeMillis();
	public int currentStage = 1;

	boolean lineClearAnim = false;
	long lineClearAnimStartMs = 0;
	int pendingLineClearCount = 0;
	int pendingBossGarbageHole = -1;
	int pieceLockSequence = 0;

	public GameEngine(GameEventBus bus) {
		this.bus = bus;
		this.mode = GameMode.SINGLE;
		sessions = new ArrayList<>();
		sessions.add(new GameSession(0));
	}

	public GameEngine() {
		this(new GameEventBus());
	}

	public GameEventBus getBus() {
		return bus;
	}

	public GameSession getSession(int idx) {
		return sessions.get(idx);
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public boolean isGameOver() {
		return gameOverSent;
	}

	// AC-05-2 연출 중 이동·회전·홀드 무시
	public boolean isInputBlocked() {
		return lineClearAnim;
	}

	public boolean isLineClearAnim() {
		return lineClearAnim;
	}

	public boolean isBlinkPhaseOn() {
		if (!lineClearAnim)
			return false;
		long elapsed = System.currentTimeMillis() - lineClearAnimStartMs;
		return (elapsed / 150) % 2 == 0;
	}

	public void start() {
		startWithSeed(System.currentTimeMillis());
	}

	public void startWithSeed(long seed) {
		isPaused = false;
		isStarted = true;
		isFallingFinished = false;
		gameOverSent = false;
		lineClearAnim = false;
		pendingLineClearCount = 0;
		pendingBossGarbageHole = -1;
		pieceLockSequence++;
		GameSession s = sessions.get(0);
		s.clearBoard();
		bagSeed = seed;
		s.randomizer = new SevenBagRandomizer(bagSeed, PentominoPolicy.defaults());
		s.randomizer.setStage(currentStage);
		s.curPiece = new Shape();
		if (!s.spawnPiece())
			handleGameOver();
	}

	public long getBagSeed() {
		return bagSeed;
	}

	public void setStage(int stage) {
		if (stage < 1)
			stage = 1;
		if (stage > MAX_STAGE)
			stage = MAX_STAGE;
		currentStage = stage;
		for (int i = 0; i < sessions.size(); i++) {
			if (sessions.get(i).randomizer != null)
				sessions.get(i).randomizer.setStage(currentStage);
		}
	}

	public int getStage() {
		return currentStage;
	}

	public int getPieceLockSequence() {
		return pieceLockSequence;
	}

	/** 네트워크 스냅샷·복원 시 줄 삭제 연출 상태를 끈다. */
	public void resetLineClearPresentation() {
		lineClearAnim = false;
		pendingLineClearCount = 0;
		pendingBossGarbageHole = -1;
		sessions.get(0).clearLineClearBlink();
	}

	public void pause() {
		if (!isStarted)
			return;
		isPaused = !isPaused;
	}

	// Board Timer 400ms 마다
	public void tick() {
		if (!isStarted || isPaused)
			return;

		if (lineClearAnim) {
			if (System.currentTimeMillis() - lineClearAnimStartMs >= LINE_CLEAR_ANIM_MS)
				finishLineClearAnim();
			return;
		}

		GameSession s = sessions.get(0);
		if (s.curPiece.getShape() == Tetrominoes.NoShape && !isFallingFinished)
			return;

		if (isFallingFinished) {
			isFallingFinished = false;
			if (!s.spawnPiece())
				handleGameOver();
		} else {
			if (!s.oneLineDown())
				onPieceLanded(s);
		}
	}

	void onPieceLanded(GameSession s) {
		pieceLockSequence++;
		s.unlockHold();
		s.lockPieceToBoard();
		bus.publish(new GameEvent(GameEventType.PIECE_LOCKED, s.playerIndex, 0));

		int toClear = s.prepareLineClearAnimation();
		if (toClear > 0) {
			pendingLineClearCount = toClear;
			lineClearAnim = true;
			lineClearAnimStartMs = System.currentTimeMillis();
			s.hideCurrentPiece();
			bus.publish(new GameEvent(GameEventType.LINE_CLEAR_ANIM_START, s.playerIndex, toClear));
			return;
		}

		if (!s.spawnPiece())
			handleGameOver();
	}

	void finishLineClearAnim() {
		GameSession s = sessions.get(0);
		int cleared = s.commitLineClear();
		if (cleared > 0) {
			s.linesRemoved += cleared;
			s.score += ScoreCalculator.forLines(cleared, currentStage);
			bus.publish(new GameEvent(GameEventType.LINES_CLEARED, s.playerIndex, cleared));
		}
		lineClearAnim = false;
		pendingLineClearCount = 0;
		bus.publish(new GameEvent(GameEventType.LINE_CLEAR_ANIM_END, s.playerIndex, cleared));
		if (pendingBossGarbageHole >= 0) {
			int hole = pendingBossGarbageHole;
			pendingBossGarbageHole = -1;
			if (!s.raiseGarbageLine(hole)) {
				handleGameOver();
				return;
			}
		}

		if (!s.spawnPiece())
			handleGameOver();
	}

	void handleGameOver() {
		isStarted = false;
		lineClearAnim = false;
		if (!gameOverSent) {
			gameOverSent = true;
			bus.publish(new GameEvent(GameEventType.GAME_OVER, 0, 0));
		}
	}

	/** C파트 보스 연동용 (추후 BossBrain에서 호출) */
	public void endByBossAttack() {
		handleGameOver();
	}

	public boolean raiseGarbageLine(int hole) {
		if (!isStarted)
			return false;
		if (lineClearAnim) {
			pendingBossGarbageHole = hole;
			return true;
		}
		if (!sessions.get(0).raiseGarbageLine(hole)) {
			handleGameOver();
			return false;
		}
		return true;
	}

	public void replaceCurrentPiece(Tetrominoes type) {
		if (!isStarted || type == null || type == Tetrominoes.NoShape)
			return;
		GameSession session = sessions.get(0);
		if (!session.spawnShape(type))
			handleGameOver();
	}

	public boolean moveLeft() {
		if (isInputBlocked())
			return false;
		return moveSession(sessions.get(0), sessions.get(0).curX - 1, sessions.get(0).curY);
	}

	public boolean moveRight() {
		if (isInputBlocked())
			return false;
		GameSession s = sessions.get(0);
		return moveSession(s, s.curX + 1, s.curY);
	}

	public boolean rotateLeft() {
		if (isInputBlocked())
			return false;
		GameSession s = sessions.get(0);
		boolean ok = s.tryRotate(false);
		if (ok)
			bus.publish(new GameEvent(GameEventType.PIECE_ROTATED, s.playerIndex, 0));
		return ok;
	}

	public boolean rotateRight() {
		if (isInputBlocked())
			return false;
		GameSession s = sessions.get(0);
		boolean ok = s.tryRotate(true);
		if (ok)
			bus.publish(new GameEvent(GameEventType.PIECE_ROTATED, s.playerIndex, 1));
		return ok;
	}

	public void softDropOnce() {
		if (isInputBlocked())
			return;
		GameSession s = sessions.get(0);
		if (!s.oneLineDown()) {
			onPieceLanded(s);
		} else {
			s.score += ScoreCalculator.forSoftDrop(1);
		}
	}

	public void hardDrop() {
		if (isInputBlocked())
			return;
		GameSession s = sessions.get(0);
		int fromY = s.curY;
		s.hardDrop();
		int cells = fromY - s.curY;
		if (cells > 0)
			s.score += ScoreCalculator.forHardDrop(cells);
		onPieceLanded(s);
	}

	public void hold() {
		if (isInputBlocked())
			return;
		GameSession s = sessions.get(0);
		if (s.holdLocked || s.curPiece.getShape() == Tetrominoes.NoShape)
			return;
		if (!s.tryHold() && s.lastHoldTopOut)
			handleGameOver();
	}

	boolean moveSession(GameSession s, int nx, int ny) {
		return s.tryMove(s.curPiece, nx, ny);
	}

	boolean moveSession(GameSession s, int nx, int ny, Shape rotated) {
		return s.tryMove(rotated, nx, ny);
	}
}
