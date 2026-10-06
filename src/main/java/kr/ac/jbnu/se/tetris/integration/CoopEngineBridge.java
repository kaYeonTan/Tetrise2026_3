package kr.ac.jbnu.se.tetris.integration;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.CoopGameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.core.PlayerAction;
import kr.ac.jbnu.se.tetris.core.ScoreCalculator;
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
	private int guestAppliedGarbageSeq;

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
		wireHostEvents();
		engine.startCoop(stage, seed);
		drivingHost = true;
		guestSyncedStage = stage;
		syncHudFromEngine();
	}

	/** 참가자: 내 보드는 로컬 풀 시뮬, 점수·보스·스테이지는 스냅샷(방장) 권위. */
	public void startOnlineGuest(int stage, long seed) {
		engine = new CoopGameEngine();
		wireGuestEvents();
		engine.startCoop(stage, seed);
		drivingHost = false;
		guestSyncedStage = -1;
		guestDropTimer = 0;
		guestAppliedGarbageSeq = 0;
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
	
	private int getDropSpeed() {
		int stage = panel.stage;
		int base = Math.max(400, 1000 - (stage - 1) * 150);
		int grav = panel.gravityMultiplier <= 0 ? 1 : panel.gravityMultiplier;
		return Math.max(1, base / grav);
	}

	public void applyAction(int playerIndex, PlayerAction action) {
		if (!isDrivingHost() || action == null) {
			return;
		}
		engine.applyAction(playerIndex, action);
		syncHudFromEngine();
	}

	/** 참가자: 내 보드 입력을 로컬 엔진에 반영 (방장에는 네트워크로 전달). */
	public void applyGuestOptimistic(int playerIndex, PlayerAction action) {
		if (drivingHost || engine == null || action == null) {
			return;
		}
		engine.applyAction(playerIndex, action);
	}

	/** 참가자: 내 플레이어 보드만 중력·줄삭제 등 엔진 틱. */
	public void tickGuestLocal(int localPlayerIndex) {
		if (drivingHost || engine == null || localPlayerIndex < 0 || localPlayerIndex > 1) {
			return;
		}
		int dropSpeed = getDropSpeed();
		guestDropTimer += 16;
		if (guestDropTimer >= dropSpeed) {
			guestDropTimer = 0;
			engine.engine(localPlayerIndex).tick();
		}
	}

	/** Applies the stage 2 boss garbage row to both authoritative online boards. */
	public boolean raiseGarbageLines(int player0Hole, int player1Hole) {
		if (!isDrivingHost()) {
			return false;
		}
		boolean player0Raised = engine.engine(0).raiseGarbageLine(player0Hole);
		boolean player1Raised = engine.engine(1).raiseGarbageLine(player1Hole);
		if (player0Raised && player1Raised) {
			panel.garbageSeq++;
			panel.garbageHole0 = player0Hole;
			panel.garbageHole1 = player1Hole;
		}
		return player0Raised && player1Raised;
	}

	/** 참가자가 실제로 지운 줄. 방장 복사 보드는 점수·HP에 넣지 않는다. */
	public void creditGuestLines(int count) {
		if (!isDrivingHost() || count <= 0) {
			return;
		}
		GameSession guest = engine.session(1);
		guest.linesRemoved += count;
		guest.score += ScoreCalculator.forLines(count, panel.stage);
		syncHudFromEngine();
		panel.applyBossLineDamage(count);
		panel.playLineClearSound(count);
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
		wireHostEvents();
		engine.restoreCoop(memento);
		drivingHost = true;
		guestSyncedStage = memento.stage;
		syncHudFromEngine();
	}

	private void wireHostEvents() {
		engine.getBus().subscribe(GameEventType.PIECE_LOCKED, e -> {
			panel.playBlockLockSound();
			panel.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.value <= 0) {
				return;
			}
			if (e.playerIndex == 0) {
				panel.applyBossLineDamage(e.value);
				panel.playLineClearSound(e.value);
			} else {
				GameSession guest = engine.session(1);
				guest.linesRemoved -= e.value;
				guest.score -= ScoreCalculator.forLines(e.value, panel.stage);
				if (guest.linesRemoved < 0) {
					guest.linesRemoved = 0;
				}
				if (guest.score < 0) {
					guest.score = 0;
				}
			}
			syncHudFromEngine();
			panel.notifyBossBoardChanged();
		});
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> panel.notifyEngineGameOver());
	}

	private void wireGuestEvents() {
		engine.getBus().subscribe(GameEventType.PIECE_LOCKED, e -> {
			if (e.playerIndex == panel.localPlayerIndex) {
				panel.playBlockLockSound();
				panel.notifyBossBoardChanged();
			}
		});
		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.playerIndex == panel.localPlayerIndex && e.value > 0) {
				panel.playLineClearSound(e.value);
				panel.reportOnlineLinesCleared(e.value);
				panel.notifyBossBoardChanged();
			}
		});
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> {
			if (e.playerIndex == panel.localPlayerIndex) {
				panel.notifyEngineGameOver();
			}
		});
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
		panel.stageAdvancePrompt = m.stageAdvancePrompt;
		panel.stageReadyPlayer0 = m.stageReadyPlayer0;
		panel.stageReadyPlayer1 = m.stageReadyPlayer1;
		if (m.stageAdvancePrompt) {
			panel.ensureNextStagePreviewLoaded(m.stage + 1);
		}
		panel.blizzardEnd = m.blizzardEnd;
		panel.onlineCoopId = m.coopId;
		panel.lastOnlineRoomId = m.lastRoomId;
		panel.pendingCoopResume = m.onlineResume ? m : null;
		panel.bossPattern.startStage(m.stage, panel, m.blueprintIndex, m.blueprintIndexP1);
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
			startOnlineGuest(mem.stage, mem.seed);
		}
		drivingHost = false;

		boolean stageChanged = guestSyncedStage != mem.stage;
		int local = panel.localPlayerIndex;
		int remote = 1 - local;
		SessionMemento remoteMem = remote == 0 ? mem.player0 : mem.player1;

		if (stageChanged) {
			engine.applyMementoState(mem);
			guestSyncedStage = mem.stage;
			guestDropTimer = 0;
			guestAppliedGarbageSeq = mem.garbageSeq;
		} else if (!mem.stageClear && !mem.stageAdvancePrompt) {
			engine.applyPlayerMemento(remote, remoteMem);
			if (mem.garbageSeq != guestAppliedGarbageSeq) {
				int hole = local == 0 ? mem.garbageHole0 : mem.garbageHole1;
				if (hole >= 0 && hole < GameSession.WIDTH) {
					engine.engine(local).raiseGarbageLine(hole);
				}
				guestAppliedGarbageSeq = mem.garbageSeq;
			}
		}

		panel.stage = mem.stage;
		panel.bossHp = mem.bossHp;
		panel.bossMax = mem.bossMax;
		panel.bossTimer = mem.bossTimer;
		panel.gameTime = mem.gameTime;
		if (mem.stageClear && !panel.stageClear) {
			panel.clearTimer = 2000;
		}
		panel.stageClear = mem.stageClear;
		panel.stageAdvancePrompt = mem.stageAdvancePrompt;
		panel.stageReadyPlayer0 = mem.stageReadyPlayer0;
		panel.stageReadyPlayer1 = mem.stageReadyPlayer1;
		if (mem.stageAdvancePrompt) {
			panel.ensureNextStagePreviewLoaded(mem.stage + 1);
		}
		panel.blizzardEnd = mem.blizzardEnd;
		panel.applyOnlineBossSync(mem);
		syncHudFromHostMem(mem);
	}

	private void syncHudFromHostMem(CoopGameMemento mem) {
		panel.p1Score = mem.player0.score;
		panel.p1Lines = mem.player0.linesRemoved;
		panel.p2Score = mem.player1.score;
		panel.p2Lines = mem.player1.linesRemoved;
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
