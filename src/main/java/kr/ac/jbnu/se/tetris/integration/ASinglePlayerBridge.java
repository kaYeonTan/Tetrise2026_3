package kr.ac.jbnu.se.tetris.integration;

import javax.swing.JLabel;

import kr.ac.jbnu.se.tetris.Board;
import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.HoldPreviewPanel;
import kr.ac.jbnu.se.tetris.NextPreviewPanel;
import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.event.GameEventType;

/**
 * 1인 플레이: B {@link GamePanel} 화면 위에 A {@link Board}/{@link GameEngine} 연결.
 */
public final class ASinglePlayerBridge {

	private static final int BOARD_X = 490;
	private static final int BOARD_Y = 104;
	private static final int BOARD_W = 300;
	private static final int BOARD_H = 600;
	private static final int HOLD_X = 356;
	private static final int HOLD_Y = 104;
	private static final int HOLD_W = 116;
	private static final int HOLD_H = 104;
	private static final int NEXT_X = 808;
	private static final int NEXT_Y = 104;
	private static final int NEXT_W = 116;
	private static final int NEXT_H = 400;

	private final GamePanel host;
	private final JLabel hiddenStatus = new JLabel(" ");
	private Board board;
	private HoldPreviewPanel holdPreview;
	private NextPreviewPanel nextPreview;
	private boolean attached;

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
		holdPreview = new HoldPreviewPanel(board.getEngine());
		nextPreview = new NextPreviewPanel(board.getEngine());
		board.setNextPreview(nextPreview);
		board.setHoldPreview(holdPreview);

		GameEngine engine = board.getEngine();
		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.value > 0) {
				host.applyBossLineDamage(e.value);
			}
			syncHud();
		});
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> host.notifyEngineGameOver());

		host.add(board);
		host.add(holdPreview);
		host.add(nextPreview);
		layoutComponents();
		attached = true;
		setVisible(false);
	}

	public void setVisible(boolean visible) {
		if (!attached) {
			return;
		}
		board.setVisible(visible);
		holdPreview.setVisible(visible);
		nextPreview.setVisible(visible);
		if (visible) {
			layoutComponents();
			host.setComponentZOrder(board, 0);
			host.setComponentZOrder(holdPreview, 0);
			host.setComponentZOrder(nextPreview, 0);
		}
	}

	private void layoutComponents() {
		board.setBounds(BOARD_X, BOARD_Y, BOARD_W, BOARD_H);
		holdPreview.setBounds(HOLD_X, HOLD_Y, HOLD_W, HOLD_H);
		nextPreview.setBounds(NEXT_X, NEXT_Y, NEXT_W, NEXT_H);
	}

	public void startStage(int stage) {
		if (!attached) {
			attach();
		}
		setVisible(true);
		GameEngine engine = board.getEngine();
		engine.setStage(stage);
		board.start();
		syncHud();
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
		if (!attached || !board.isVisible()) {
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
