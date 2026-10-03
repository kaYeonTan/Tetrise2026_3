package kr.ac.jbnu.se.tetris.integration;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.CoopGameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.core.PlayerAction;
import kr.ac.jbnu.se.tetris.event.GameEventType;
import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshot;
import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshotMapper;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;

/** 온라인 방장: CoopGameEngine 2세션이 권위, GamePanel은 HUD·보스·렌더. */
public final class CoopEngineBridge {

	private final GamePanel panel;
	private CoopGameEngine engine;
	private boolean drivingHost;

	public CoopEngineBridge(GamePanel panel) {
		this.panel = panel;
	}

	public boolean isDrivingHost() {
		return drivingHost && engine != null;
	}

	public CoopGameEngine getEngine() {
		return engine;
	}

	public void startOnlineHost(int stage, long seed) {
		engine = new CoopGameEngine();
		engine.getBus().subscribe(GameEventType.PIECE_LOCKED, e -> {
			panel.playBlockLockSound();
			panel.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.value > 0) {
				panel.applyBossLineDamage(e.value);
				panel.playLineClearSound(e.value);
			}
			syncHudFromEngine();
			panel.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> panel.notifyEngineGameOver());
		engine.startCoop(stage, seed);
		drivingHost = true;
		syncHudFromEngine();
	}

	public void stop() {
		drivingHost = false;
		engine = null;
	}

	public void tickHost() {
		if (!isDrivingHost()) {
			return;
		}
		engine.tickCoop();
		syncHudFromEngine();
	}

	public void applyAction(int playerIndex, PlayerAction action) {
		if (!isDrivingHost() || action == null) {
			return;
		}
		engine.applyAction(playerIndex, action);
		syncHudFromEngine();
	}

	/** Applies the stage 2 boss garbage row to both authoritative online boards. */
	public boolean raiseGarbageLines(int player0Hole, int player1Hole) {
		if (!isDrivingHost()) {
			return false;
		}
		boolean player0Raised = engine.engine(0).raiseGarbageLine(player0Hole);
		boolean player1Raised = engine.engine(1).raiseGarbageLine(player1Hole);
		return player0Raised && player1Raised;
	}

	public CoopGameMemento createMemento(String coopId) {
		if (engine == null) {
			return null;
		}
		return engine.createMemento(panel, coopId);
	}

	public void restoreAndDrive(CoopGameMemento memento) {
		engine = new CoopGameEngine();
		wireEvents();
		engine.restoreCoop(memento);
		drivingHost = true;
		syncHudFromEngine();
	}

	private void wireEvents() {
		engine.getBus().subscribe(GameEventType.PIECE_LOCKED, e -> {
			panel.playBlockLockSound();
			panel.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.value > 0) {
				panel.applyBossLineDamage(e.value);
				panel.playLineClearSound(e.value);
			}
			syncHudFromEngine();
			panel.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> panel.notifyEngineGameOver());
	}

	public void syncHudFromEngine() {
		if (engine == null) {
			return;
		}
		panel.p1Score = engine.session(0).score;
		panel.p1Lines = engine.session(0).linesRemoved;
		panel.p2Score = engine.session(1).score;
		panel.p2Lines = engine.session(1).linesRemoved;
	}

	/** 네트워크 스냅샷 전 panel 보드 동기화. */
	public void syncBoardsToPanel() {
		if (!isDrivingHost()) {
			return;
		}
		sessionCellsToPanelBoard(engine.session(0), panel.board1);
		sessionCellsToPanelBoard(engine.session(1), panel.board2);
	}

	public static void applyLoadedMemento(GamePanel panel, CoopGameMemento m) {
		panel.isTwoPlayer = true;
		panel.stage = m.stage;
		panel.bossHp = m.bossHp;
		panel.bossMax = m.bossMax;
		panel.bossTimer = m.bossTimer;
		panel.gameTime = m.gameTime;
		panel.stageClear = m.stageClear;
		panel.blizzardEnd = m.blizzardEnd;
		panel.onlineCoopId = m.coopId;
		panel.lastOnlineRoomId = m.lastRoomId;
		panel.pendingCoopResume = m.onlineResume ? m : null;
		panel.bossPattern.startStage(m.stage, panel, m.blueprintIndex);
		if (m.player0 != null && m.player1 != null) {
			if (panel.coopEngineBridge == null) {
				panel.coopEngineBridge = new CoopEngineBridge(panel);
			}
			panel.coopEngineBridge.restoreAndDrive(m);
		} else if (m.panelSnapshotJson != null && !m.panelSnapshotJson.isBlank()) {
			var snap = new com.google.gson.Gson().fromJson(m.panelSnapshotJson,
					kr.ac.jbnu.se.tetris.network.coop.CoopSnapshot.class);
			CoopSnapshotMapper.applyToPanel(snap, panel, true);
		}
		panel.screen = 5;
		panel.startBattleBgm();
	}

	public GameSession session(int player) {
		return isDrivingHost() ? engine.session(player) : null;
	}

	public boolean isLineClearBlinkOn(int player) {
		if (!isDrivingHost()) {
			return false;
		}
		GameEngineAdapter a = new GameEngineAdapter(engine.engine(player));
		return a.isLineClearBlinkOn();
	}

	static final class GameEngineAdapter {
		private final kr.ac.jbnu.se.tetris.core.GameEngine e;

		GameEngineAdapter(kr.ac.jbnu.se.tetris.core.GameEngine e) {
			this.e = e;
		}

		boolean isLineClearBlinkOn() {
			return e.isLineClearAnim() && e.isBlinkPhaseOn();
		}
	}

	public static int panelRotFromSession(GameSession s) {
		// HUD 미니맵용 — SRS 상태를 대략 0~3으로 (렌더는 엔진 보드 사용)
		return 0;
	}

	public static void sessionCellsToPanelBoard(GameSession s, int[][] board24) {
		for (int r = 0; r < 24; r++) {
			for (int c = 0; c < 10; c++) {
				board24[r][c] = 0;
			}
		}
		for (int ey = 0; ey < GameSession.HEIGHT; ey++) {
			int pr = ey + 4;
			if (pr >= 24) {
				continue;
			}
			for (int x = 0; x < GameSession.WIDTH; x++) {
				Tetrominoes shape = s.shapeAt(x, ey);
				if (shape != Tetrominoes.NoShape) {
					board24[pr][x] = GamePanel.engineBlockValue(shape);
				}
			}
		}
	}
}
