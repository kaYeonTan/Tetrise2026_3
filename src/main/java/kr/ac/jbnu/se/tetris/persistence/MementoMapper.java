package kr.ac.jbnu.se.tetris.persistence;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.CoopGameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.piece.RotationState;

/** GameSession / CoopGameEngine ↔ Memento. */
public final class MementoMapper {

	private MementoMapper() {
	}

	public static SessionMemento fromSession(GameSession s) {
		SessionMemento m = new SessionMemento();
		m.playerIndex = s.playerIndex;
		m.score = s.score;
		m.linesRemoved = s.linesRemoved;
		m.cells = new String[GameSession.WIDTH * GameSession.HEIGHT];
		for (int y = 0; y < GameSession.HEIGHT; y++) {
			for (int x = 0; x < GameSession.WIDTH; x++) {
				m.cells[y * GameSession.WIDTH + x] = s.shapeAt(x, y).name();
			}
		}
		m.activeShape = s.curPiece.getShape().name();
		m.activeRotation = s.curPiece.getRotationState().name();
		int blocks = s.curPiece.blockCount();
		m.activeCoords = new int[blocks * 2];
		for (int i = 0; i < blocks; i++) {
			m.activeCoords[i * 2] = s.curPiece.x(i);
			m.activeCoords[i * 2 + 1] = s.curPiece.y(i);
		}
		m.curX = s.curX;
		m.curY = s.curY;
		m.holdShape = s.hold.getStored().name();
		m.holdLocked = s.holdLocked;
		if (s.randomizer != null) {
			m.randomizer = s.randomizer.saveState();
		}
		return m;
	}

	public static void restoreSession(GameSession s, SessionMemento m) {
		if (m == null) {
			return;
		}
		s.score = m.score;
		s.linesRemoved = m.linesRemoved;
		for (int y = 0; y < GameSession.HEIGHT; y++) {
			for (int x = 0; x < GameSession.WIDTH; x++) {
				String name = m.cells[y * GameSession.WIDTH + x];
				s.cells[y * GameSession.WIDTH + x] = Tetrominoes.valueOf(name);
			}
		}
		s.curPiece.setShape(Tetrominoes.valueOf(m.activeShape));
		if (m.activeRotation != null && !m.activeRotation.isBlank()) {
			try {
				s.curPiece.setRotationState(RotationState.valueOf(m.activeRotation));
			} catch (IllegalArgumentException ignored) {
				// legacy snapshot without valid rotation
			}
		}
		if (m.activeCoords != null && m.activeCoords.length >= 2) {
			int blocks = m.activeCoords.length / 2;
			int[][] layout = new int[blocks][2];
			for (int i = 0; i < blocks; i++) {
				layout[i][0] = m.activeCoords[i * 2];
				layout[i][1] = m.activeCoords[i * 2 + 1];
			}
			s.curPiece.applyLocalCoords(layout);
		}
		s.curX = m.curX;
		s.curY = m.curY;
		s.hold.setStored(Tetrominoes.valueOf(m.holdShape));
		s.holdLocked = m.holdLocked;
		if (m.randomizer != null && s.randomizer != null) {
			s.randomizer.restoreState(m.randomizer);
		}
	}

	public static CoopGameMemento fromEngine(CoopGameEngine engine, GamePanel panel, String coopId) {
		CoopGameMemento m = new CoopGameMemento();
		m.coopId = coopId;
		m.seed = engine.getBaseSeed();
		m.stage = panel.stage;
		m.bossHp = panel.bossHp;
		m.bossMax = panel.bossMax;
		m.bossTimer = panel.bossTimer;
		m.gameTime = panel.gameTime;
		m.stageClear = panel.stageClear;
		m.gameOver = panel.screen == 7
				|| (panel.coopEliminated0 && panel.coopEliminated1);
		m.player0Eliminated = panel.coopEliminated0;
		m.player1Eliminated = panel.coopEliminated1;
		m.stageAdvancePrompt = panel.stageAdvancePrompt;
		m.stageReadyPlayer0 = panel.stageReadyPlayer0;
		m.stageReadyPlayer1 = panel.stageReadyPlayer1;
		m.blizzardEnd = panel.blizzardEnd;
		m.blueprintIndex = panel.bossPattern.blueprintIndex();
		panel.fillOnlineBossSyncFields(m);
		m.player0 = fromSession(engine.session(0));
		m.player0.pieceLockSequence = engine.engine(0).getPieceLockSequence();
		m.player1 = fromSession(engine.session(1));
		m.player1.pieceLockSequence = engine.engine(1).getPieceLockSequence();
		m.onlineResume = panel.isOnlineCoop;
		m.lastRoomId = panel.lastOnlineRoomId;
		m.savedAtMs = System.currentTimeMillis();
		return m;
	}

	public static void applySharedCoopProgressToPanel(GamePanel panel, CoopGameMemento m) {
		if (panel == null || m == null) {
			return;
		}
		panel.stage = m.stage;
		panel.bossHp = m.bossHp;
		panel.bossMax = m.bossMax;
		panel.bossTimer = m.bossTimer;
		panel.gameTime = m.gameTime;
		if (m.stageClear && !panel.stageClear) {
			panel.clearTimer = 2000;
		}
		panel.stageClear = m.stageClear;
		panel.stageAdvancePrompt = m.stageAdvancePrompt;
		panel.stageReadyPlayer0 = m.stageReadyPlayer0;
		panel.stageReadyPlayer1 = m.stageReadyPlayer1;
		if (m.stageAdvancePrompt) {
			panel.ensureNextStagePreviewLoaded(m.stage + 1);
		}
		panel.blizzardEnd = m.blizzardEnd;
		panel.applyOnlineBossSync(m);
	}

	public static void applyHudFromCoopMemento(GamePanel panel, CoopGameMemento m) {
		if (panel == null || m == null || m.player0 == null || m.player1 == null) {
			return;
		}
		panel.p1Score = m.player0.score;
		panel.p1Lines = m.player0.linesRemoved;
		panel.p2Score = m.player1.score;
		panel.p2Lines = m.player1.linesRemoved;
	}

	public static void applyCoopResumeShellToPanel(GamePanel panel, CoopGameMemento m) {
		if (panel == null || m == null) {
			return;
		}
		panel.isTwoPlayer = true;
		panel.onlineCoopId = m.coopId;
		panel.lastOnlineRoomId = m.lastRoomId;
		panel.pendingCoopResume = m.onlineResume ? m : null;
		panel.bossPattern.startStage(m.stage, panel, m.blueprintIndex);
		applySharedCoopProgressToPanel(panel, m);
	}

}
