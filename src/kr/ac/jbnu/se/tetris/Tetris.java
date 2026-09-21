package kr.ac.jbnu.se.tetris;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Tetris extends JFrame {

	JLabel statusbar;

	public Tetris() {

		//스코어 표시
		statusbar = new JLabel(" 0");
		add(statusbar, BorderLayout.SOUTH);
		Board board = new Board(this);
		add(board);
		board.start();

		//실제 테트리스 패널의 가로세로 크기
		setSize(200, 400);
		//테트리스 타이틀
		setTitle("Tetris");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public JLabel getStatusBar() {
		return statusbar;
	}

	public static void main(String[] args) {
		Tetris game = new Tetris();
		game.setLocationRelativeTo(null);
		game.setVisible(true);
	}
}