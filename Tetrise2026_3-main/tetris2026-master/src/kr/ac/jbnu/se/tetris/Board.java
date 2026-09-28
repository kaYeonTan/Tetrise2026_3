package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

	public final int BoardWidth = 10;
	public final int BoardHeight = 22;

	public Timer timer;
	public boolean isFallingFinished = false;
	public boolean isStarted = false;
	public boolean isPaused = false;
	public int numLinesRemoved = 0;
	public int curX = 0;
	public int curY = 0;
	public boolean isTwoPlayer = false;
	public boolean isP2FallingFinished = false;
	public int p2X = 0;
	public int p2Y = 0;
	public JLabel statusbar;
	public Shape curPiece;
	public Shape p2Piece;
	public Tetrominoes[] board;

	// 보드 생성
	public Board(Tetris parent) {
		setFocusable(true);
		curPiece = new Shape();
		p2Piece = new Shape();
		timer = new Timer(400, this);
		timer.start();
		statusbar = parent.getStatusBar();
		board = new Tetrominoes[BoardWidth * BoardHeight];
		addKeyListener(new TAdapter());
		clearBoard();
	}

	// 게임 루프
	public void actionPerformed(ActionEvent e) {
		if (isFallingFinished) {
			isFallingFinished = false;
			newPiece();
		} else {
			oneLineDown();
		}
		if (isTwoPlayer) {
			if (isP2FallingFinished) {
				isP2FallingFinished = false;
				newPieceP2();
			} else {
				oneLineDownP2();
			}
		}
	}

	// 보드 크기 계산
	public int squareWidth() {
		return (int) getSize().getWidth() / BoardWidth;
	}

	public int squareHeight() {
		return (int) getSize().getHeight() / BoardHeight;
	}

	public Tetrominoes shapeAt(int x, int y) {
		return board[(y * BoardWidth) + x];
	}

	// 시작
	public void start() {
		startGame(false);
	}

	public void startTwoPlayer() {
		startGame(true);
	}

	public void startGame(boolean twoPlayer) {
		if (isPaused)
			return;
		if (twoPlayer) {
			isTwoPlayer = true;
		} else {
			isTwoPlayer = false;
		}
		isStarted = true;
		isFallingFinished = false;
		isP2FallingFinished = false;
		numLinesRemoved = 0;
		clearBoard();
		newPiece();
		if (isTwoPlayer) {
			newPieceP2();
		}
		timer.start();
	}

	// 일시정지
	public void pause() {
		if (!isStarted)
			return;
		isPaused = !isPaused;
		if (isPaused) {
			timer.stop();
			statusbar.setText("paused");
		} else {
			timer.start();
			statusbar.setText(String.valueOf(numLinesRemoved));
		}
		repaint();
	}

	// 화면 그리기
	public void paint(Graphics g) {
		super.paint(g);
		Dimension size = getSize();
		int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();
		for (int i = 0; i < BoardHeight; ++i) {
			for (int j = 0; j < BoardWidth; ++j) {
				Tetrominoes shape = shapeAt(j, BoardHeight - i - 1);
				if (shape != Tetrominoes.NoShape) {
					drawSquare(g, 0 + j * squareWidth(), boardTop + i * squareHeight(), shape);
				}
			}
		}
		if (curPiece.getShape() != Tetrominoes.NoShape) {
			for (int i = 0; i < 4; ++i) {
				int x = curX + curPiece.x(i);
				int y = curY - curPiece.y(i);
				if (x >= 0 && x < BoardWidth) {
					if (y >= 0 && y < BoardHeight) {
						drawSquare(g, 0 + x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(), curPiece.getShape());
					}
				}
			}
		}
		if (isTwoPlayer && p2Piece.getShape() != Tetrominoes.NoShape) {
			for (int i = 0; i < 4; ++i) {
				int x = p2X + p2Piece.x(i);
				int y = p2Y - p2Piece.y(i);
				if (x >= 0 && x < BoardWidth) {
					if (y >= 0 && y < BoardHeight) {
						drawSquare(g, x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(), p2Piece.getShape());
					}
				}
			}
		}
	}

	// 블록 내리기
	public void dropDown() {
		int newY = curY;
		while (newY > 0) {
			if (!tryMove(curPiece, curX, newY - 1)) {
				break;
			}
			--newY;
		}
		pieceDropped();
	}

	// 한 줄 내리기
	public void oneLineDown() {
		if (!tryMove(curPiece, curX, curY - 1)) {
			pieceDropped();
		}
	}

	public void dropDownP2() {
		int newY = p2Y;
		while (newY > 0) {
			if (!tryMoveP2(p2Piece, p2X, newY - 1)) {
				break;
			}
			--newY;
		}
		pieceDroppedP2();
	}

	public void oneLineDownP2() {
		if (!tryMoveP2(p2Piece, p2X, p2Y - 1)) {
			pieceDroppedP2();
		}
	}

	// 보드 비우기
	public void clearBoard() {
		for (int i = 0; i < BoardHeight * BoardWidth; ++i) {
			board[i] = Tetrominoes.NoShape;
		}
	}

	// 블록 고정
	public void pieceDropped() {
		for (int i = 0; i < 4; ++i) {
			int x = curX + curPiece.x(i);
			int y = curY - curPiece.y(i);
			board[(y * BoardWidth) + x] = curPiece.getShape();
		}
		removeFullLines(false);
		if (!isFallingFinished) {
			newPiece();
		}
	}

	public void newPiece() {
		curPiece.setRandomShape();
		curX = isTwoPlayer ? 2 : BoardWidth / 2 + 1;
		curY = BoardHeight - 1 + curPiece.minY();
		if (!tryMove(curPiece, curX, curY)) {
			curPiece.setShape(Tetrominoes.NoShape);
			timer.stop();
			isStarted = false;
			statusbar.setText("game over");
		}
	}

	public void newPieceP2() {
		p2Piece.setRandomShape();
		p2X = BoardWidth - 3;
		p2Y = BoardHeight - 1 + p2Piece.minY();
		if (!tryMoveP2(p2Piece, p2X, p2Y)) {
			p2Piece.setShape(Tetrominoes.NoShape);
			timer.stop();
			isStarted = false;
			statusbar.setText("game over");
		}
	}

	// 이동 체크
	public boolean tryMove(Shape newPiece, int newX, int newY) {
		for (int i = 0; i < 4; ++i) {
			int x = newX + newPiece.x(i);
			int y = newY - newPiece.y(i);
			if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight) {
				return false;
			}
			if (shapeAt(x, y) != Tetrominoes.NoShape) {
				return false;
			}
		}
		curPiece = newPiece;
		curX = newX;
		curY = newY;
		repaint();
		return true;
	}

	public boolean tryMoveP2(Shape newPiece, int newX, int newY) {
		for (int i = 0; i < 4; ++i) {
			int x = newX + newPiece.x(i);
			int y = newY - newPiece.y(i);
			if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight) {
				return false;
			}
			if (shapeAt(x, y) != Tetrominoes.NoShape) {
				return false;
			}
		}
		p2Piece = newPiece;
		p2X = newX;
		p2Y = newY;
		repaint();
		return true;
	}

	// 줄 삭제
	public void removeFullLines(boolean p2) {
		int numFullLines = 0;
		for (int i = BoardHeight - 1; i >= 0; --i) {
			boolean lineIsFull = true;
			for (int j = 0; j < BoardWidth; ++j) {
				if (shapeAt(j, i) == Tetrominoes.NoShape) {
					lineIsFull = false;
					break;
				}
			}
			if (lineIsFull) {
				++numFullLines;
				for (int k = i; k < BoardHeight - 1; ++k) {
					for (int j = 0; j < BoardWidth; ++j) {
						board[(k * BoardWidth) + j] = shapeAt(j, k + 1);
					}
				}
			}
		}
		if (numFullLines > 0) {
			numLinesRemoved += numFullLines;
			statusbar.setText(String.valueOf(numLinesRemoved));
			if (p2) {
				isP2FallingFinished = true;
				p2Piece.setShape(Tetrominoes.NoShape);
			} else {
				isFallingFinished = true;
				curPiece.setShape(Tetrominoes.NoShape);
			}
			repaint();
		}
	}

	public void pieceDroppedP2() {
		for (int i = 0; i < 4; ++i) {
			int x = p2X + p2Piece.x(i);
			int y = p2Y - p2Piece.y(i);
			board[(y * BoardWidth) + x] = p2Piece.getShape();
		}
		removeFullLines(true);
		if (!isP2FallingFinished) {
			newPieceP2();
		}
	}

	// 사각형 그리기
	public void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
		Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
				new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204),
				new Color(218, 170, 0) };
		Color color = colors[shape.ordinal()];
		g.setColor(color);
		g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
		g.setColor(color.brighter());
		g.drawLine(x, y + squareHeight() - 1, x, y);
		g.drawLine(x, y, x + squareWidth() - 1, y);
		g.setColor(color.darker());
		g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
		g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
	}

	class TAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape) {
				return;
			}
			int keycode = e.getKeyCode();
			if (keycode == 'p' || keycode == 'P') {
				pause();
				return;
			}
			if (isPaused) {
				return;
			}
			switch (keycode) {
			case KeyEvent.VK_LEFT:
				tryMove(curPiece, curX - 1, curY);
				break;
			case KeyEvent.VK_RIGHT:
				tryMove(curPiece, curX + 1, curY);
				break;
			case KeyEvent.VK_DOWN:
				tryMove(curPiece.rotateRight(), curX, curY);
				break;
			case KeyEvent.VK_UP:
				tryMove(curPiece.rotateLeft(), curX, curY);
				break;
			case KeyEvent.VK_SPACE:
				dropDown();
				break;
			case 'a':
			case 'A':
				if (isTwoPlayer) {
					tryMoveP2(p2Piece, p2X - 1, p2Y);
				}
				break;
			case 'd':
			case 'D':
				if (isTwoPlayer) {
					tryMoveP2(p2Piece, p2X + 1, p2Y);
				} else {
					oneLineDown();
				}
				break;
			case 'w':
			case 'W':
				if (isTwoPlayer) {
					tryMoveP2(p2Piece.rotateLeft(), p2X, p2Y);
				}
				break;
			case 's':
			case 'S':
				if (isTwoPlayer) {
					oneLineDownP2();
				}
				break;
			case 'f':
			case 'F':
				if (isTwoPlayer) {
					dropDownP2();
				}
				break;
			default:
				if (isTwoPlayer) {
					if (keycode == 0) {
						if (isPaused) {
							return;
						}
					}
				}
			}
		}
	}
}