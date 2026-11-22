package kr.ac.jbnu.se.tetris.app;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import kr.ac.jbnu.se.tetris.audio.SoundManager;
import kr.ac.jbnu.se.tetris.repository.Repositories;
import kr.ac.jbnu.se.tetris.ui.router.Route;
import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;
import kr.ac.jbnu.se.tetris.ui.screen.StagePanelScreen;

/**
 * 통합 실행 진입점.
 * ScreenRouter 기반으로 화면 전환을 관리하며, 현재는 StagePanelScreen(GamePanel)이
 * 모든 화면 로직을 담당한다. 이후 TitleScreen, ModeSelectScreen 등으로 점진적 분리 예정.
 */
public class GameWindow {

    private static SoundManager soundManager;
    private static ScreenRouter router;

    public static void main(String[] args) {
        Repositories repos = Repositories.at(Path.of("data"));
        javax.swing.SwingUtilities.invokeLater(() -> createAndShow(repos));
    }

    static void createAndShow(Repositories repos) {
        JFrame frame = new JFrame("TETRIS · 스테이지 보스 러시");
        soundManager = new SoundManager();
        soundManager.preload();

        // ScreenRouter 초기화. AuthGuard는 로그인 세션 통합 후 추가한다.
        // 지금은 GamePanel 내부에서 loginUser로 관리한다.
        router = new ScreenRouter(frame, List.of());

        // StagePanelScreen: 기존 GamePanel 전체를 Screen으로 감싸서 등록
        // GamePanel이 내부적으로 screen 변수로 타이틀/게임/설정 등 모든 화면 처리
        StagePanelScreen stageScreen = new StagePanelScreen(soundManager, repos);
        router.register(Route.STAGE_RUSH, () -> stageScreen);

        // UI 폰트 설정
        Font f = stageScreen.getPanel().baseFont.deriveFont(16.5f);
        UIManager.put("OptionPane.messageFont", f);
        UIManager.put("OptionPane.buttonFont", f);

        // 프레임 설정
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // 초기 화면: STAGE_RUSH (GamePanel이 타이틀부터 시작)
        router.resetTo(Route.STAGE_RUSH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        stageScreen.getPanel().requestFocusInWindow();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                soundManager.close();
            }
        });
    }

    /** 현재 라우터 반환 (다른 컴포넌트에서 화면 전환 시 사용) */
    public static ScreenRouter getRouter() {
        return router;
    }

    /** 사운드 매니저 반환 */
    public static SoundManager getSoundManager() {
        return soundManager;
    }
}
