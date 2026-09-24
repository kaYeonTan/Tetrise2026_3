package kr.ac.jbnu.se.tetris;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TETRIS · 스테이지 보스 러시");
            GamePanel panel = new GamePanel();
            // 알림창 글씨체
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
        });
    }
}
