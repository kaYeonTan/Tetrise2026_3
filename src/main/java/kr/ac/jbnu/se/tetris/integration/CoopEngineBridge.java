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
import kr.ac.jbnu.se.tetris.persistence.SessionMemento;

/** 온라인 방장: CoopGameEngine 2세션이 권위, GamePanel은 HUD·보스·렌더. */
public final class CoopEngineBridge {

	private final GamePanel panel;
	private CoopGameEngine engine;
	private boolean drivingHost;
	
	// 낙하 속도 조절용 타이머 (학부생 스타일: 간단한 카운터)
	private int dropTimer0 = 0;  // P1 낙하 타이머
	private int dropTimer1 = 0;  // P2 낙하 타이머
	private int guestDropTimer = 0;
	private int guestSyncedStage = -1;
	private int guestAppliedLocalLockSeq = -1;

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
		// 학부생 스타일: 간단한 시간 기반 낙하
		// 스테이지에 따라 낙하 속도가 달라짐 (1000ms → 400ms)
		int dropSpeed = getDropSpeed();
		
		// 16ms마다 호출되니까 타이머 증가
		dropTimer0 += 16;
		dropTimer1 += 16;
		
		// P1 낙하
		if (dropTimer0 >= dropSpeed) {
			dropTimer0 = 0;
			engine.engine(0).tick();
		}
		
		// P2 낙하
		if (dropTimer1 >= dropSpeed) {
			dropTimer1 = 0;
			engine.engine(1).tick();
		}
		
		syncHudFromEngine();
	}
	
	// 스테이지에 따른 낙하 속도 계산 (간단 버전)
	private int getDropSpeed() {
		int stage = panel.stage;
		// 스테이지 1: 1000ms, 스테이지 5: 400ms
		return Math.max(400, 1000 - (stage - 1) * 150);
	}

	public void applyAction(int playerIndex, PlayerAction action) {
		if (!isDrivingHost() || action == null) {
			return;
		}
		engine.applyAction(playerIndex, action);
		syncHudFromEngine();
	}

	/** 참가자: 내 보드 입력을 스냅샷 도착 전 로컬 엔진에 먼저 반영한다. */
	public void applyGuestOptimistic(int playerIndex, PlayerAction action) {
		if (drivingHost || engine == null || action == null) {
			return;
		}
		engine.applyAction(playerIndex, action);
		syncHudForPlayer(playerIndex);
	}

	/** 참가자: 내 플레이어 보드만 중력 틱(방장과 동일 간격). */
	public void tickGuestPrediction(int localPlayerIndex) {
		if (drivingHost || engine == null || localPlayerIndex < 0 || localPlayerIndex > 1) {
			return;
		}
		int dropSpeed = getDropSpeed();
		guestDropTimer += 16;
		if (guestDropTimer >= dropSpeed) {
			guestDropTimer = 0;
			engine.engine(localPlayerIndex).tick();
			syncHudForPlayer(localPlayerIndex);
		}
	}

	private void syncHudForPlayer(int playerIndex) {
		if (playerIndex == 0) {
			panel.p1Score = engine.session(0).score;
			panel.p1Lines = engine.session(0).linesRemoved;
		} else {
			panel.p2Score = engine.session(1).score;
			panel.p2Lines = engine.session(1).linesRemoved;
		}
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

	public void restartStageEngine(int stage) {
		if (!isDrivingHost() || engine == null) {
			return;
		}
		engine.restartStage(stage);
		dropTimer0 = 0;
		dropTimer1 = 0;
		syncHudFromEngine();
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
		return engine != null ? engine.session(player) : null;
	}

	public boolean hasRenderEngine() {
		return engine != null;
	}

	public void syncGuestFromMemento(CoopGameMemento mem) {
		if (mem == null || mem.player0 == null || mem.player1 == null) {
			return;
		}
		if (engine == null) {
			engine = new CoopGameEngine();
		}
		drivingHost = false;

		boolean stageChanged = guestSyncedStage != mem.stage;
		int local = panel.localPlayerIndex;
		int remote = 1 - local;
		SessionMemento remoteMem = remote == 0 ? mem.player0 : mem.player1;
		SessionMemento localMem = local == 0 ? mem.player0 : mem.player1;

		engine.engine(0).resetLineClearPresentation();
		engine.engine(1).resetLineClearPresentation();

		if (stageChanged) {
			engine.applyMementoState(mem);
			guestAppliedLocalLockSeq = localMem.pieceLockSequence;
			guestSyncedStage = mem.stage;
			guestDropTimer = 0;
		} else {
			engine.applyPlayerMemento(remote, remoteMem);
			boolean needLocal = localMem.pieceLockSequence != guestAppliedLocalLockSeq;
			if (needLocal) {
				engine.applyPlayerMemento(local, localMem);
				guestAppliedLocalLockSeq = localMem.pieceLockSequence;
				guestDropTimer = 0;
			}
		}

		panel.stage = mem.stage;
		panel.bossHp = mem.bossHp;
		panel.bossMax = mem.bossMax;
		panel.bossTimer = mem.bossTimer;
		panel.gameTime = mem.gameTime;
		panel.stageClear = mem.stageClear;
		panel.blizzardEnd = mem.blizzardEnd;
		panel.applyOnlineBossSync(mem);
		syncHudFromEngine();
	}

	public boolean isLineClearBlinkOn(int player) {
		if (engine == null) {
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
			int pr = 23 - ey;
			if (pr < 0 || pr >= 24) {
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
