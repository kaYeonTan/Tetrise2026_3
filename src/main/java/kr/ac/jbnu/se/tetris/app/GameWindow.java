package kr.ac.jbnu.se.tetris.app;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.audio.SoundManager;

/*
 * 통합 실행 진입점 — B파트 GamePanel UI(타이틀·메뉴·스테이지·설정 등)를 그대로 사용.
 * A파트 엔진/라우터(SRS·ScreenRouter)는 이후 GamePanel 화면과 단계적으로 연결 예정.
 */
public class GameWindow {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(GameWindow::createAndShow);
    }

    static void createAndShow() {
        JFrame frame = new JFrame("TETRIS · 스테이지 보스 러시");
        SoundManager soundManager = new SoundManager();
        soundManager.preload();
        GamePanel panel = new GamePanel(soundManager);

        Font f = panel.font.deriveFont(16.5f);
        UIManager.put("OptionPane.messageFont", f);
        UIManager.put("OptionPane.buttonFont", f);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                soundManager.close();
            }
        });
    }
}
