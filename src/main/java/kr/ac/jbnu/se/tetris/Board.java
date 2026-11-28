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



import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.event.GameEventType;
import kr.ac.jbnu.se.tetris.view.RenderOptions;



// 보드 그리기 + 키 입력. 로직은 GameEngine

public class Board extends JPanel implements ActionListener {



	final int BoardWidth = GameSession.WIDTH;

	final int BoardHeight = GameSession.HEIGHT;



	Timer timer;

	JLabel statusbar;

	GameEngine engine;

	NextPreviewPanel nextPreview;

	HoldPreviewPanel holdPreview;

	Runnable pauseOverlayHandler;
	int gravityMultiplier = 1;
	int activePieceGravityMultiplier = 1;
	int nextPieceGravityMultiplier = 1;
	int baseGravityDelayMillis = 400;



	public Board(JLabel statusbar) {

		this.statusbar = statusbar;
		// GamePanel already paints the board background and grid.  The single-player
		// board is layered on top of it, so it must not erase that shared artwork.
		setOpaque(false);

		setFocusable(true);

		engine = new GameEngine();

		// 나중에 C 보스/사운드가 여기 subscribe 할 예정

		engine.getBus().subscribe(GameEventType.GAME_OVER, e -> {

			statusbar.setText("game over");

		});

		engine.getBus().subscribe(GameEventType.LINES_CLEARED, e -> updateStatus());

		timer = new Timer(400, this);

		engine.getBus().subscribe(GameEventType.LINE_CLEAR_ANIM_START, e -> timer.setDelay(100));

		engine.getBus().subscribe(GameEventType.LINE_CLEAR_ANIM_END, e -> updateTimerDelay());
		engine.getBus().subscribe(GameEventType.PIECE_LOCKED, e -> {
			activePieceGravityMultiplier = nextPieceGravityMultiplier;
			nextPieceGravityMultiplier = 1;
			updateTimerDelay();
		});



		addKeyListener(new TAdapter());

	}

	public void setPauseOverlayHandler(Runnable handler) {
		pauseOverlayHandler = handler;
	}

	public void ensurePaused() {
		if (!engine.isStarted() || engine.isPaused())
			return;
		pause();
	}

	public void resumeFromPause() {
		if (engine.isStarted() && engine.isPaused())
			pause();
	}

	public GameEngine getEngine() {
		return engine;
	}

	public void setGravityMultiplier(int multiplier) {
		gravityMultiplier = Math.max(1, multiplier);
		updateTimerDelay();
	}

	public void setBaseGravityDelay(int delayMillis) {
		int delay = Math.max(1, delayMillis);
		if (baseGravityDelayMillis != delay) {
			baseGravityDelayMillis = delay;
			updateTimerDelay();
		}
	}

	public void setNextPieceGravityMultiplier(int multiplier) {
		nextPieceGravityMultiplier = Math.max(1, multiplier);
	}

	public void resetBossGravity() {
		gravityMultiplier = 1;
		activePieceGravityMultiplier = 1;
		nextPieceGravityMultiplier = 1;
		updateTimerDelay();
	}

	private void updateTimerDelay() {
		if (engine.isLineClearAnim())
			return;
		int multiplier = Math.max(gravityMultiplier, activePieceGravityMultiplier);
		int delay = Math.max(1, baseGravityDelayMillis / multiplier);
		timer.setDelay(delay);
		timer.setInitialDelay(delay);
	}

	public void setNextPreview(NextPreviewPanel panel) {
		nextPreview = panel;
	}

	public void setHoldPreview(HoldPreviewPanel panel) {
		holdPreview = panel;
	}

	void refreshNextPreview() {
		if (nextPreview != null)
			nextPreview.repaint();
	}

	void refreshHoldPreview() {
		if (holdPreview != null)
			holdPreview.repaint();
	}

	void refreshSidePanels() {
		refreshNextPreview();
		refreshHoldPreview();
	}



	public void actionPerformed(ActionEvent e) {

		if (!engine.isStarted()) {

			timer.stop();

			return;

		}

		engine.tick();

		if (!engine.isStarted())

			timer.stop();

		updateStatus();

		repaint();

		refreshSidePanels();

	}



	void updateStatus() {

		if (!engine.isPaused() && engine.isStarted()) {
			GameSession ses = engine.getSession(0);
			statusbar.setText("sc" + ses.score + "  ln" + ses.linesRemoved + "  st" + engine.getStage());
		}

	}



	int squareWidth() {

		return (int) getSize().getWidth() / BoardWidth;

	}



	int squareHeight() {

		return (int) getSize().getHeight() / BoardHeight;

	}



	public void start() {

		engine.start();
		updateTimerDelay();

		timer.restart();

		updateStatus();

		repaint();

		refreshSidePanels();

	}



	private void pause() {

		if (!engine.isStarted())

			return;



		engine.pause();

		if (engine.isPaused()) {

			timer.stop();

			statusbar.setText("paused");

		} else {

			timer.start();

			updateStatus();

		}

		repaint();

		refreshSidePanels();

	}



	public void paint(Graphics g) {

		super.paint(g);



		GameSession s = engine.getSession(0);

		Dimension size = getSize();

		int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();



		boolean gray = engine.isGameOver();

		drawLockedBlocks(g, s, boardTop, gray);

		if (s.curPiece.getShape() != Tetrominoes.NoShape) {
			drawCurrentPiece(g, s, boardTop, gray);
		}

		if (gray) {
			g.setColor(Color.DARK_GRAY);
			g.drawString("GAME OVER", 24, boardTop + 40);
		}

	}

	private void drawLockedBlocks(Graphics g, GameSession s, int boardTop, boolean gray) {
		boolean blinkOn = engine.isLineClearAnim() && engine.isBlinkPhaseOn();

		for (int i = 0; i < BoardHeight; ++i) {
			int boardY = BoardHeight - i - 1;
			boolean blinkRow = engine.isLineClearAnim() && s.lineClearBlinkRows[boardY];

			for (int j = 0; j < BoardWidth; ++j) {
				Tetrominoes shape = s.shapeAt(j, boardY);
				if (shape == Tetrominoes.NoShape) {
					continue;
				}
				int x = j * squareWidth();
				int y = boardTop + i * squareHeight();
				if (blinkRow && blinkOn) {
					g.setColor(Color.WHITE);
					g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
				} else {
					drawSquare(g, x, y, shape, gray);
				}
			}
		}
	}

	private void drawCurrentPiece(Graphics g, GameSession s, int boardTop, boolean gray) {
		int ghostY = s.ghostLandingY();

		// 착지 위치 미리보기 (현재랑 같으면 생략)
		int n = s.curPiece.blockCount();
		if (ghostY != s.curY) {
			for (int i = 0; i < n; ++i) {
				int x = s.curX + s.curPiece.x(i);
				int y = ghostY - s.curPiece.y(i);
				drawGhostSquare(g, x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(),
						s.curPiece.getShape());
			}
		}

		for (int i = 0; i < n; ++i) {
			int x = s.curX + s.curPiece.x(i);
			int y = s.curY - s.curPiece.y(i);
			drawSquare(g, x * squareWidth(), boardTop + (BoardHeight - y - 1) * squareHeight(),
					s.curPiece.getShape(), gray);
		}
	}



	// 낙하 끝날 자리 (테두리만)
	private void drawGhostSquare(Graphics g, int x, int y, Tetrominoes shape) {
		Color color = PieceColors.of(shape);
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
		g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 55));
		g.drawRect(x + 1, y + 1, squareWidth() - 3, squareHeight() - 3);
	}

	private void drawSquare(Graphics g, int x, int y, Tetrominoes shape, boolean grayscale) {

		Color color = RenderOptions.pieceColor(PieceColors.of(shape), grayscale);
		int width = squareWidth();
		int height = squareHeight();

		// Match GamePanel.drawCell(): dark outer cell, inset body, bright top/left.
		g.setColor(color.darker());
		g.fillRect(x, y, width, height);
		g.setColor(color);
		g.fillRect(x + 2, y + 2, width - 4, height - 4);
		g.setColor(color.brighter());
		g.fillRect(x + 2, y + 2, width - 4, 3);
		g.fillRect(x + 2, y + 2, 3, height - 4);

	}



	class TAdapter extends KeyAdapter {

		public void keyPressed(KeyEvent e) {



			GameSession s = engine.getSession(0);

			if (!engine.isStarted() || s.curPiece.getShape() == Tetrominoes.NoShape) {

				return;

			}



			int keycode = e.getKeyCode();



			if (keycode == 'p' || keycode == 'P') {

				if (pauseOverlayHandler != null)
					pauseOverlayHandler.run();
				else
					pause();

				return;

			}



			if (engine.isPaused())

				return;

			if (engine.isInputBlocked())

				return;



			switch (keycode) {

			case KeyEvent.VK_LEFT:

				engine.moveLeft();

				break;

			case KeyEvent.VK_RIGHT:

				engine.moveRight();

				break;

			case KeyEvent.VK_DOWN:

				engine.rotateRight();

				break;

			case KeyEvent.VK_UP:

				engine.rotateLeft();

				break;

			case KeyEvent.VK_SPACE:

				engine.hardDrop();

				break;

			case 'd':

			case 'D':

				engine.softDropOnce();

				break;

			case 'c':

			case 'C':

				engine.hold();

				break;

			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
				// 테스트용 스테이지 (펜토는 3+)
				engine.setStage(keycode - '0');
				break;

			default:
				break;

			}

			updateStatus();

			repaint();

			refreshSidePanels();

		}

	}

}


