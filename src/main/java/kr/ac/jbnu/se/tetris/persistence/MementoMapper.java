package kr.ac.jbnu.se.tetris.persistence;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.CoopGameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;

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
		m.blizzardEnd = panel.blizzardEnd;
		m.blueprintIndex = panel.bossPattern.blueprintIndex();
		m.player0 = fromSession(engine.session(0));
		m.player1 = fromSession(engine.session(1));
		m.onlineResume = panel.isOnlineCoop;
		m.lastRoomId = panel.lastOnlineRoomId;
		m.savedAtMs = System.currentTimeMillis();
		return m;
	}

}
