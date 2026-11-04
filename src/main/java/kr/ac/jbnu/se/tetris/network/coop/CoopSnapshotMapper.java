package kr.ac.jbnu.se.tetris.network.coop;

import java.util.ArrayList;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.Player;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.CoopRunState;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import kr.ac.jbnu.se.tetris.persistence.SessionMemento;

/** GamePanel ↔ CoopSnapshot 변환. */
public final class CoopSnapshotMapper {

	private CoopSnapshotMapper() {
	}

	public static CoopSnapshot fromPanel(GamePanel p, String coopId) {
		CoopSnapshot s = new CoopSnapshot();
		s.coopId = coopId;
		CoopRunState run = new CoopRunState();
		run.readFrom(p);
		s.stage = run.stage;
		s.bossHp = run.bossHp;
		s.bossMax = run.bossMax;
		s.bossTimer = run.bossTimer;
		s.gameTime = run.gameTime;
		s.stageClear = run.stageClear;
		s.blizzardEnd = run.blizzardEnd;
		s.blueprintIndex = p.bossPattern.blueprintIndex();
		fillPlayer(s.p1, p, true);
		fillPlayer(s.p2, p, false);
		return s;
	}

	static void fillPlayer(CoopPlayerSnapshot dst, GamePanel p, boolean p1) {
		Player pl = p.players[p1 ? 0 : 1];
		dst.type = pl.type;
		dst.rot = pl.rot;
		dst.x = pl.x;
		dst.y = pl.y;
		dst.hold = pl.hold;
		dst.holdUsed = pl.holdUsed;
		dst.score = pl.score;
		dst.lines = pl.lines;
		dst.garbage = pl.garbage;
		dst.next = new ArrayList<>(pl.next);
		dst.bag = new ArrayList<>(pl.bag);
		dst.board = boardToString(pl.board);
	}

	public static void applyToPanel(CoopSnapshot s, GamePanel p, boolean fullStageReset) {
		if (fullStageReset) {
			p.newGame(true);
			p.startStage(s.blueprintIndex);
		}
		p.stage = s.stage;
		p.bossHp = s.bossHp;
		p.bossMax = s.bossMax;
		p.bossTimer = s.bossTimer;
		p.gameTime = s.gameTime;
		p.stageClear = s.stageClear;
		p.blizzardEnd = s.blizzardEnd;
		applyPlayer(s.p1, p, true);
		applyPlayer(s.p2, p, false);
	}

	static void applyPlayer(CoopPlayerSnapshot src, GamePanel p, boolean p1) {
		Player pl = p.players[p1 ? 0 : 1];
		pl.type = src.type;
		pl.rot = src.rot;
		pl.x = src.x;
		pl.y = src.y;
		pl.hold = src.hold;
		pl.holdUsed = src.holdUsed;
		pl.score = src.score;
		pl.lines = src.lines;
		pl.garbage = src.garbage;
		pl.next.clear();
		pl.next.addAll(src.next);
		pl.bag.clear();
		pl.bag.addAll(src.bag);
		stringToBoard(src.board, pl.board);
	}

	static String boardToString(int[][] board) {
		StringBuilder sb = new StringBuilder(240);
		for (int r = 0; r < 24; r++) {
			for (int c = 0; c < 10; c++) {
				sb.append(board[r][c]);
			}
		}
		return sb.toString();
	}

	public static CoopSnapshot fromMemento(CoopGameMemento m) {
		if (m == null) {
			return null;
		}
		CoopSnapshot s = new CoopSnapshot();
		s.coopId = m.coopId;
		s.stage = m.stage;
		s.bossHp = m.bossHp;
		s.bossMax = m.bossMax;
		s.bossTimer = m.bossTimer;
		s.gameTime = m.gameTime;
		s.stageClear = m.stageClear;
		s.blizzardEnd = m.blizzardEnd;
		s.blueprintIndex = m.blueprintIndex;
		fillFromSessionMemento(s.p1, m.player0);
		fillFromSessionMemento(s.p2, m.player1);
		return s;
	}

	static void fillFromSessionMemento(CoopPlayerSnapshot dst, SessionMemento sm) {
		if (sm == null) {
			return;
		}
		dst.score = sm.score;
		dst.lines = sm.linesRemoved;
		dst.holdUsed = sm.holdLocked;
		dst.board = sessionMementoToBoardString(sm);
		Tetrominoes active = Tetrominoes.valueOf(sm.activeShape);
		dst.type = GamePanel.engineType(active);
		dst.x = sm.curX;
		dst.y = sm.curY + 4;
		Tetrominoes hold = Tetrominoes.valueOf(sm.holdShape);
		dst.hold = hold == Tetrominoes.NoShape ? -1 : GamePanel.engineType(hold);
		dst.rot = 0;
	}

	static String sessionMementoToBoardString(SessionMemento sm) {
		StringBuilder sb = new StringBuilder(240);
		for (int pr = 0; pr < 24; pr++) {
			for (int x = 0; x < 10; x++) {
				if (pr < 4) {
					sb.append('0');
					continue;
				}
				int ey = pr - 4;
				int v = 0;
				if (sm.cells != null && ey < GameSession.HEIGHT) {
					Tetrominoes t = Tetrominoes.valueOf(sm.cells[ey * GameSession.WIDTH + x]);
					if (t != Tetrominoes.NoShape) {
						v = GamePanel.engineBlockValue(t);
					}
				}
				sb.append(v);
			}
		}
		return sb.toString();
	}

	static void stringToBoard(String s, int[][] board) {
		if (s == null || s.length() < 240) {
			return;
		}
		int i = 0;
		for (int r = 0; r < 24; r++) {
			for (int c = 0; c < 10; c++) {
				board[r][c] = s.charAt(i++) - '0';
			}
		}
	}
}
