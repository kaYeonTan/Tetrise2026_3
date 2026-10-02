package kr.ac.jbnu.se.tetris.ui.screen;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import kr.ac.jbnu.se.tetris.ui.router.Route;
import kr.ac.jbnu.se.tetris.ui.router.RouteParams;
import kr.ac.jbnu.se.tetris.ui.router.SavePurpose;
import kr.ac.jbnu.se.tetris.ui.router.SaveSlotParams;
import kr.ac.jbnu.se.tetris.ui.router.Screen;
import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;

public class PauseOverlayScreen implements Screen {

	ScreenRouter router;
	GameScreen gameScreen;
	JPanel root;

	public PauseOverlayScreen(ScreenRouter router, GameScreen gameScreen) {
		this.router = router;
		this.gameScreen = gameScreen;
		root = new JPanel(new GridLayout(0, 1, 6, 6));
		root.setBackground(new Color(0, 0, 0, 160));
		root.setBorder(new EmptyBorder(40, 40, 40, 40));
		root.add(new JLabel("PAUSE"));
		JButton resume = new JButton("Resume");
		resume.addActionListener(e -> {
			router.back();
			gameScreen.resumeFromPause();
		});
		root.add(resume);
		JButton save = new JButton("Save (슬롯)");
		save.addActionListener(e -> router.navigate(Route.SAVE_SLOTS, new SaveSlotParams(SavePurpose.SAVE)));
		root.add(save);
		JButton title = new JButton("Title");
		title.addActionListener(e -> router.navigate(Route.TITLE));
		root.add(title);
	}

	@Override
	public JPanel getRoot() {
		return root;
	}

	@Override
	public void onEnter(RouteParams params) {
		gameScreen.getBoard().ensurePaused();
	}

	@Override
	public void onExit() {
	}
}
