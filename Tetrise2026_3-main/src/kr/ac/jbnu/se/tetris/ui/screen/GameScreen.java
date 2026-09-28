package kr.ac.jbnu.se.tetris.ui.screen;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import kr.ac.jbnu.se.tetris.Board;
import kr.ac.jbnu.se.tetris.HoldPreviewPanel;
import kr.ac.jbnu.se.tetris.NextPreviewPanel;
import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.core.GameMode;
import kr.ac.jbnu.se.tetris.ui.router.GameParams;
import kr.ac.jbnu.se.tetris.ui.router.Route;
import kr.ac.jbnu.se.tetris.ui.router.RouteParams;
import kr.ac.jbnu.se.tetris.ui.router.Screen;
import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;

public class GameScreen implements Screen {

	ScreenRouter router;
	JPanel root = new JPanel(new BorderLayout());
	JLabel statusbar = new JLabel(" 0");
	Board board;
	boolean started;

	public GameScreen(ScreenRouter router) {
		this.router = router;
		board = new Board(statusbar);
		NextPreviewPanel nextPanel = new NextPreviewPanel(board.getEngine());
		HoldPreviewPanel holdPanel = new HoldPreviewPanel(board.getEngine());
		board.setNextPreview(nextPanel);
		board.setHoldPreview(holdPanel);
		board.setPauseOverlayHandler(() -> {
			if (!board.getEngine().isStarted())
				return;
			board.ensurePaused();
			router.navigate(Route.PAUSE);
		});
		root.add(holdPanel, BorderLayout.WEST);
		root.add(board, BorderLayout.CENTER);
		root.add(nextPanel, BorderLayout.EAST);
		root.add(statusbar, BorderLayout.SOUTH);
	}

	public Board getBoard() {
		return board;
	}

	public GameEngine getEngine() {
		return board.getEngine();
	}

	@Override
	public JPanel getRoot() {
		return root;
	}

	@Override
	public void onEnter(RouteParams params) {
		GameParams gp = params instanceof GameParams ? (GameParams) params : new GameParams(GameMode.SINGLE);
		if (!gp.resume) {
			board.getEngine().setStage(gp.stage);
			board.start();
			started = true;
		}
		board.requestFocusInWindow();
	}

	public void resumeFromPause() {
		board.resumeFromPause();
		board.requestFocusInWindow();
	}

	@Override
	public void onExit() {
	}
}
