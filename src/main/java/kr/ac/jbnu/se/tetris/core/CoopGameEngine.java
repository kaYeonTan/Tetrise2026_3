package kr.ac.jbnu.se.tetris.core;

import kr.ac.jbnu.se.tetris.event.GameEventBus;
import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import kr.ac.jbnu.se.tetris.persistence.MementoMapper;
import kr.ac.jbnu.se.tetris.persistence.SessionMemento;

/** 2세션 협동 — GameEngine 인스턴스 2개로 독립 낙하·줄삭제. */
public final class CoopGameEngine {

	private final GameEventBus bus;
	private final GameEngine player0;
	private final GameEngine player1;
	private long baseSeed;
	private boolean started;

	public CoopGameEngine() {
		bus = new GameEventBus();
		player0 = new GameEngine(bus);
		player1 = new GameEngine(bus);
		player1.getSession(0).playerIndex = 1;
	}

	public GameEventBus getBus() {
		return bus;
	}

	public GameEngine engine(int playerIndex) {
		return playerIndex == 0 ? player0 : player1;
	}

	public GameSession session(int playerIndex) {
		return engine(playerIndex).getSession(0);
	}

	public long getBaseSeed() {
		return baseSeed;
	}

	public boolean isStarted() {
		return started;
	}

	public void startCoop(int stage, long seed) {
		baseSeed = seed;
		player0.setStage(stage);
		player1.setStage(stage);
		player0.startWithSeed(seed);
		player1.startWithSeed(seed + 1L);
		started = true;
	}

	public void tickCoop() {
		if (!started) {
			return;
		}
		player0.tick();
		player1.tick();
	}

	public void applyAction(int playerIndex, PlayerAction action) {
		GameEngine e = engine(playerIndex);
		if (!acceptsInput(e)) {
			return;
		}
		dispatchAction(e, action);
	}

	private static boolean acceptsInput(GameEngine e) {
		return e.isStarted() && !e.isPaused() && !e.isInputBlocked();
	}

	private static void dispatchAction(GameEngine e, PlayerAction action) {
		switch (action) {
			case MOVE_LEFT:
				e.moveLeft();
				break;
			case MOVE_RIGHT:
				e.moveRight();
				break;
			case SOFT_DROP:
				e.softDropOnce();
				break;
			case HARD_DROP:
				e.hardDrop();
				break;
			case ROTATE_CW:
				e.rotateRight();
				break;
			case ROTATE_CCW:
				e.rotateLeft();
				break;
			case HOLD:
				e.hold();
				break;
			default:
				break;
		}
	}

	public CoopGameMemento createMemento(GamePanel panel, String coopId) {
		return MementoMapper.fromEngine(this, panel, coopId);
	}

	public void restoreCoop(CoopGameMemento m) {
		if (m == null) {
			return;
		}
		baseSeed = m.seed;
		player0.setStage(m.stage);
		player1.setStage(m.stage);
		player0.startWithSeed(m.seed);
		player1.startWithSeed(m.seed + 1L);
		MementoMapper.restoreSession(session(0), m.player0);
		MementoMapper.restoreSession(session(1), m.player1);
		started = true;
	}

	public void applyMementoState(CoopGameMemento m) {
		if (m == null) {
			return;
		}
		if (!started) {
			restoreCoop(m);
			return;
		}
		applyPlayerMemento(0, m.player0);
		applyPlayerMemento(1, m.player1);
	}

	public void applyPlayerMemento(int playerIndex, SessionMemento memento) {
		if (memento == null) {
			return;
		}
		engine(playerIndex).resetLineClearPresentation();
		MementoMapper.restoreSession(session(playerIndex), memento);
	}

	public void restartStage(int stage) {
		long seed = baseSeed ^ ((long) stage * 997L);
		startCoop(stage, seed);
	}

}
