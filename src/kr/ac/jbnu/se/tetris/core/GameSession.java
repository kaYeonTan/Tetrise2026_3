package kr.ac.jbnu.se.tetris.core;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.piece.HoldSlot;
import kr.ac.jbnu.se.tetris.piece.SevenBagRandomizer;
import kr.ac.jbnu.se.tetris.piece.SrsRotationSystem;

// 플레이어 1명분 보드 + 현재 블록 (2인이면 이 클래스 2개)
public class GameSession {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 22;

	public int playerIndex;
	public Tetrominoes[] cells;
	public Shape curPiece;
	public int curX;
	public int curY;
	public int linesRemoved;
	public int score;
	public SevenBagRandomizer randomizer;
	public HoldSlot hold = new HoldSlot();
	public boolean holdLocked = false;
	public boolean lastHoldTopOut = false;
	/** 줄 삭제 연출 중 깜빡일 행 (board y, 0=바닥) */
	public final boolean[] lineClearBlinkRows = new boolean[HEIGHT];

	public GameSession(int playerIndex) {
		this.playerIndex = playerIndex;
		cells = new Tetrominoes[WIDTH * HEIGHT];
		curPiece = new Shape();
		clearBoard();
	}

	public void clearBoard() {
		for (int i = 0; i < cells.length; i++)
			cells[i] = Tetrominoes.NoShape;
		linesRemoved = 0;
		score = 0;
		hold.clear();
		holdLocked = false;
		clearLineClearBlink();
	}

	public void clearLineClearBlink() {
		for (int i = 0; i < lineClearBlinkRows.length; i++)
			lineClearBlinkRows[i] = false;
	}

	public void unlockHold() {
		holdLocked = false;
	}

	// AC-03-2 스폰 방향(회전 0)
	public boolean spawnShape(Tetrominoes type) {
		curPiece.setShape(type);
		curX = WIDTH / 2 + 1;
		curY = HEIGHT - 1 + curPiece.minY();
		return tryMove(curPiece, curX, curY);
	}

	public Tetrominoes shapeAt(int x, int y) {
		return cells[(y * WIDTH) + x];
	}

	// 이동 가능한지 (보드만 검사, 상태는 안 바꿈)
	public boolean canPlace(Shape piece, int newX, int newY) {
		for (int i = 0; i < piece.blockCount(); ++i) {
			int x = newX + piece.x(i);
			int y = newY - piece.y(i);
			if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
				return false;
			if (shapeAt(x, y) != Tetrominoes.NoShape)
				return false;
		}
		return true;
	}

	// AC-00-1 충돌·범위 체크
	public boolean tryMove(Shape newPiece, int newX, int newY) {
		if (!canPlace(newPiece, newX, newY))
			return false;
		curPiece = newPiece;
		curX = newX;
		curY = newY;
		return true;
	}

	// 고스트(낙하 미리보기)용 착지 curY
	public int ghostLandingY() {
		if (curPiece.getShape() == Tetrominoes.NoShape)
			return curY;
		int gy = curY;
		while (canPlace(curPiece, curX, gy - 1))
			gy--;
		return gy;
	}

	// 바닥 닿으면 보드에 박음
	public void lockPieceToBoard() {
		for (int i = 0; i < curPiece.blockCount(); ++i) {
			int x = curX + curPiece.x(i);
			int y = curY - curPiece.y(i);
			cells[(y * WIDTH) + x] = curPiece.getShape();
		}
	}

	public void hideCurrentPiece() {
		curPiece.setShape(Tetrominoes.NoShape);
	}

	// AC-05-1 연출 전: 가득 찬 줄 개수만 (보드는 그대로)
	public int prepareLineClearAnimation() {
		clearLineClearBlink();
		int numFullLines = 0;
		for (int i = HEIGHT - 1; i >= 0; --i) {
			if (isRowFull(i)) {
				lineClearBlinkRows[i] = true;
				numFullLines++;
			}
		}
		return numFullLines;
	}

	boolean isRowFull(int row) {
		for (int j = 0; j < WIDTH; ++j) {
			if (shapeAt(j, row) == Tetrominoes.NoShape)
				return false;
		}
		return true;
	}

	// AC-00-2 연출 후 실제 삭제
	public int commitLineClear() {
		int numFullLines = 0;
		for (int i = HEIGHT - 1; i >= 0; --i) {
			if (!isRowFull(i))
				continue;
			numFullLines++;
			for (int k = i; k < HEIGHT - 1; ++k) {
				for (int j = 0; j < WIDTH; ++j)
					cells[(k * WIDTH) + j] = shapeAt(j, k + 1);
			}
		}
		clearLineClearBlink();
		return numFullLines;
	}

	// 7-Bag에서 꺼내서 스폰
	public boolean spawnPiece() {
		if (randomizer == null)
			randomizer = new SevenBagRandomizer();
		curPiece.setShape(randomizer.next());
		curX = WIDTH / 2 + 1;
		curY = HEIGHT - 1 + curPiece.minY();
		return tryMove(curPiece, curX, curY);
	}

	public Tetrominoes peekNextBlock() {
		if (randomizer == null)
			return Tetrominoes.NoShape;
		return randomizer.peekNext();
	}

	public boolean oneLineDown() {
		if (!tryMove(curPiece, curX, curY - 1))
			return false;
		return true;
	}

	public void hardDrop() {
		while (tryMove(curPiece, curX, curY - 1)) {
			// keep falling
		}
	}

	// SRS wall kick 포함 회전
	public boolean tryRotate(boolean clockwise) {
		return SrsRotationSystem.rotate(this, clockwise);
	}

	// F-03 홀드 (C키)
	public boolean tryHold() {
		lastHoldTopOut = false;
		if (holdLocked || curPiece.getShape() == Tetrominoes.NoShape)
			return false;

		Tetrominoes active = curPiece.getShape();
		Tetrominoes inHold = hold.getStored();

		// AC-03-1 같은 블록 연속 홀드 (비어있지 않을 때 같은 종류 swap 금지)
		if (!hold.isEmpty() && inHold == active)
			return false;

		Tetrominoes backupHold = inHold;
		hold.setStored(active);

		boolean ok;
		if (backupHold == Tetrominoes.NoShape)
			ok = spawnPiece();
		else
			ok = spawnShape(backupHold);

		if (!ok) {
			lastHoldTopOut = true;
			return false;
		}

		holdLocked = true;
		return true;
	}
}
