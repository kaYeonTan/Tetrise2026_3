package kr.ac.jbnu.se.tetris.integration;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.ScreenId;
import kr.ac.jbnu.se.tetris.core.CoopGameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.core.PlayerAction;
import kr.ac.jbnu.se.tetris.core.ScoreCalculator;
import kr.ac.jbnu.se.tetris.event.GameEventType;
import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshot;
import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshotMapper;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import kr.ac.jbnu.se.tetris.persistence.MementoMapper;
import kr.ac.jbnu.se.tetris.persistence.SessionMemento;

public final class CoopEngineBridge implements GameEngineBridge {

	private final GamePanel panel;
	private CoopGameEngine engine;
	private boolean drivingHost;
	
	private long hostDropTimer;
	private long guestDropTimer;
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
		hostDropTimer = 0;
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

	public void tickHost(long deltaMillis) {
		if (!isDrivingHost()) {
			return;
		}
		if (panel.coopEliminated0) {
			syncHudFromEngine();
			return;
		}
		// P1만 낙하. P2는 참가자가 보낸 보드를 그대로 그린다.
		hostDropTimer = tickGravity(0, hostDropTimer + deltaMillis);
		syncHudFromEngine();
	}

	private int getDropSpeed() {
		int multiplier = Math.max(1, panel.gravityMultiplier);
		return Math.max(1, panel.baseDropIntervalMillis() / multiplier);
	}

	private long tickGravity(int playerIndex, long elapsedMillis) {
		var playerEngine = engine.engine(playerIndex);
		if (playerEngine.isLineClearAnim()) {
			playerEngine.tick();
			return 0;
		}
		int intervalMillis = getDropSpeed();
		if (elapsedMillis >= intervalMillis) {
			playerEngine.tick();
			return elapsedMillis % intervalMillis;
		}
		return elapsedMillis;
	}

	public void applyAction(int playerIndex, PlayerAction action) {
		if (!isDrivingHost() || action == null) {
			return;
		}
		// P2 조작은 참가자 PC에서만 계산한다
		if (playerIndex != 0) {
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
	public void tickGuestLocal(int localPlayerIndex, long deltaMillis) {
		if (drivingHost || engine == null || localPlayerIndex < 0 || localPlayerIndex > 1) {
			return;
		}
		if (panel.isCoopPlayerEliminated(localPlayerIndex)) {
			return;
		}
		guestDropTimer = tickGravity(localPlayerIndex, guestDropTimer + deltaMillis);
	}

	/** Applies the stage 2 boss garbage row to both authoritative online boards. */
	public boolean raiseGarbageLines(int player0Hole, int player1Hole) {
		if (!isDrivingHost()) {
			return false;
		}
		boolean player0Raised = engine.engine(0).raiseGarbageLine(player0Hole);
		boolean player1Raised = engine.engine(1).raiseGarbageLine(player1Hole);
		panel.applyOnlineGarbageRaise(player0Hole, player1Hole);
		return player0Raised && player1Raised;
	}

	/** 참가자가 보낸 P2 보드를 화면에만 붙인다. 점수·줄 수는 방장 값을 유지. */
	public void showGuestBoard(SessionMemento guestBoard) {
		if (!isDrivingHost() || guestBoard == null || engine == null) {
			return;
		}
		GameSession guest = engine.session(1);
		int score = guest.score;
		int lines = guest.linesRemoved;
		engine.applyPlayerMemento(1, guestBoard);
		guest.score = score;
		guest.linesRemoved = lines;
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
		hostDropTimer = 0;
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
		hostDropTimer = 0;
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
		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> panel.onCoopPlayerEliminated(e.playerIndex));
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
				panel.onCoopPlayerEliminated(e.playerIndex);
			}
		});
	}

	public void syncHudFromEngine() {
		MementoMapper.applyHudFromEngine(panel, engine);
	}

	/** 네트워크 스냅샷 전 panel 보드 동기화. */
	public void syncBoardsToPanel() {
		if (!isDrivingHost()) {
			return;
		}
		MementoMapper.applyHostBoardsFromEngine(panel, engine);
	}

	public static void applyLoadedMemento(GamePanel panel, CoopGameMemento m) {
		MementoMapper.applyCoopResumeShellToPanel(panel, m);
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
		panel.setScreen(ScreenId.GAME);
		panel.startBattleBgm();
		if (m.player0 != null && m.player1 != null && panel.coopEngineBridge != null) {
			panel.coopEngineBridge.applyEliminationFromMemento(m);
			panel.applyCoopEliminationFromSave(m.player0Eliminated, m.player1Eliminated, true);
		}
	}

	public void applyEliminationFromMemento(CoopGameMemento m) {
		if (m == null || engine == null) {
			return;
		}
		if (m.player0Eliminated) {
			freezeEngine(0);
		}
		if (m.player1Eliminated) {
			freezeEngine(1);
		}
	}

	public void freezeEngine(int playerIndex) {
		if (engine != null) {
			engine.engine(playerIndex).freezeAfterElimination();
		}
	}

	@Override
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

		MementoMapper.applySharedCoopProgressToPanel(panel, mem);
		applyEliminationFromMemento(mem);
		panel.applyCoopEliminationFromSave(mem.player0Eliminated, mem.player1Eliminated, true);
		MementoMapper.applyHudFromCoopMemento(panel, mem);
	}

	@Override
	public boolean hasActiveEngine() {
		return hasRenderEngine();
	}

	@Override
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

}
