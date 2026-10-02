package kr.ac.jbnu.se.tetris.ui.screen;

import javax.swing.JComponent;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.ui.router.RouteParams;
import kr.ac.jbnu.se.tetris.ui.router.Screen;

/**
 * B파트 GamePanel(스테이지 보스 러시)을 A파트 라우터에 연결하는 Screen.
 */
public class StagePanelScreen implements Screen {

	private final GamePanel panel;

	public StagePanelScreen() {
		this.panel = new GamePanel();
	}

	@Override
	public JComponent getRoot() {
		return panel;
	}

	@Override
	public void onEnter(RouteParams params) {
		panel.requestFocusInWindow();
	}

	@Override
	public void onExit() {
		// 필요시 GamePanel 정리 로직
	}
}
