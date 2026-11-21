package kr.ac.jbnu.se.tetris.ui.screen;

import javax.swing.JComponent;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.audio.SoundManager;
import kr.ac.jbnu.se.tetris.repository.Repositories;
import kr.ac.jbnu.se.tetris.ui.router.RouteParams;
import kr.ac.jbnu.se.tetris.ui.router.Screen;

/**
 * B파트 GamePanel(스테이지 보스 러시)을 A파트 라우터에 연결하는 Screen.
 * GamePanel이 내부적으로 타이틀/모드선택/게임/설정 등 모든 화면을 screen 변수로 관리.
 * 이후 TitleScreen, ModeSelectScreen 등으로 점진적 분리 예정.
 */
public class StagePanelScreen implements Screen {

	private final GamePanel panel;

	public StagePanelScreen(Repositories repos) {
		this(new SoundManager(), repos);
	}

	public StagePanelScreen(SoundManager soundManager, Repositories repos) {
		this.panel = new GamePanel(soundManager, repos);
	}

	/** 내부 GamePanel 반환 (폰트 설정 등 외부 접근용) */
	public GamePanel getPanel() {
		return panel;
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
