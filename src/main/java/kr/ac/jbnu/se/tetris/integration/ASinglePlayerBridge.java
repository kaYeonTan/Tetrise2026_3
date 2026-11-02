package kr.ac.jbnu.se.tetris.integration;

import javax.swing.JLabel;

import kr.ac.jbnu.se.tetris.Board;
import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.boss.brain.BossBrainRunner;
import kr.ac.jbnu.se.tetris.boss.brain.Grid;
import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.event.GameEventType;

public final class ASinglePlayerBridge implements GameEngineBridge {

	private final GamePanel host;
	private final JLabel hiddenStatus = new JLabel(" ");
	private Board board;
	private BossBrainRunner bossBrainRunner;
	private boolean attached;
	private boolean active;

	public ASinglePlayerBridge(GamePanel host) {
		this.host = host;
		hiddenStatus.setVisible(false);
	}

	public boolean isAttached() {
		return attached;
	}

	public void attach() {
		if (attached) {
			return;
		}
		board = new Board(hiddenStatus);
		board.setFocusable(false);

		GameEngine engine = board.getEngine();
		bossBrainRunner = new BossBrainRunner(engine);
		engine.getBus().subscribe(GameEventType.PIECE_LOCKED, e -> {
			host.playBlockLockSound();
			host.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.value > 0) {
				host.applyBossLineDamage(e.value);
				host.playLineClearSound(e.value);
			}
			syncHud();
			host.notifyBossBoardChanged();
			bossBrainRunner.refresh(engine);
		});
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> host.notifyEngineGameOver());

		attached = true;
		setVisible(false);
	}

	public void setVisible(boolean visible) {
		if (!attached) {
			return;
		}
		active = visible;
	}

	public GameSession session() {
		return attached && board != null ? board.getEngine().getSession(0) : null;
	}

	@Override
	public boolean hasActiveEngine() {
		return attached && board != null && board.getEngine().isStarted();
	}

	@Override
	public GameSession session(int playerIndex) {
		return playerIndex == 0 ? session() : null;
	}

	@Override
	public boolean isLineClearBlinkOn(int playerIndex) {
		return playerIndex == 0 && isLineClearBlinkOn();
	}

	public boolean isLineClearBlinkOn() {
		return attached && board != null && board.getEngine().isLineClearAnim()
				&& board.getEngine().isBlinkPhaseOn();
	}

	public void startStage(int stage, int baseGravityDelayMillis) {
		if (!attached) {
			attach();
		}
		setVisible(true);
		GameEngine engine = board.getEngine();
		engine.setStage(stage);
		board.setBaseGravityDelay(baseGravityDelayMillis);
		board.start();
		bossBrainRunner.refresh(engine);
		syncHud();
	}

	public Grid brainSnapshot() {
		return bossBrainRunner == null ? null : bossBrainRunner.latestGrid();
	}

	public BossBrainRunner.Decision brainDecision() {
		return bossBrainRunner == null ? null : bossBrainRunner.latestDecision();
	}

	public boolean raiseGarbageLine(int hole) {
		if (!attached || board == null)
			return false;
		boolean raised = board.getEngine().raiseGarbageLine(hole);
		board.repaint();
		return raised;
	}

	public void replaceCurrentPiece(Tetrominoes type) {
		if (attached && board != null) {
			board.getEngine().replaceCurrentPiece(type);
			board.repaint();
		}
	}

	public void setGravityMultiplier(int multiplier) {
		if (attached && board != null)
			board.setGravityMultiplier(multiplier);
	}

	public void setNextPieceGravityMultiplier(int multiplier) {
		if (attached && board != null)
			board.setNextPieceGravityMultiplier(multiplier);
	}

	public void resetBossEffects() {
		if (attached && board != null)
			board.resetBossGravity();
	}

	public void syncHud() {
		if (!attached || board == null) {
			return;
		}
		var s = board.getEngine().getSession(0);
		host.p1Score = s.score;
		host.p1Lines = s.linesRemoved;
	}

	public void pauseEngine() {
		if (attached && board.getEngine().isStarted() && !board.getEngine().isPaused()) {
			board.ensurePaused();
		}
	}

	public void resumeEngine() {
		if (attached && board.getEngine().isStarted() && board.getEngine().isPaused()) {
			board.resumeFromPause();
		}
	}

	public void stopAndHide() {
		setVisible(false);
	}

	public void handleKey(int code, int[] keys) {
		if (!attached || !active) {
			return;
		}
		GameEngine engine = board.getEngine();
		if (!engine.isStarted() || engine.isPaused() || engine.isInputBlocked()) {
			return;
		}
		if (code == keys[0]) {
			engine.moveLeft();
		} else if (code == keys[1]) {
			engine.moveRight();
		} else if (code == keys[2]) {
			engine.softDropOnce();
		} else if (code == keys[3]) {
			engine.hardDrop();
		} else if (code == keys[4]) {
			engine.rotateRight();
		} else if (code == keys[5]) {
			engine.rotateLeft();
		} else if (code == keys[6]) {
			engine.hold();
		} else {
			return;
		}
		syncHud();
		host.repaint();
	}
}
