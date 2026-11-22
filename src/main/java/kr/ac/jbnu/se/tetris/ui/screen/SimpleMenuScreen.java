package kr.ac.jbnu.se.tetris.ui.screen;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import kr.ac.jbnu.se.tetris.ui.router.RouteParams;
import kr.ac.jbnu.se.tetris.ui.router.Screen;

public class SimpleMenuScreen implements Screen {

	JPanel root = new JPanel(new GridLayout(0, 1, 8, 8));
	String title;

	public SimpleMenuScreen(String title) {
		this.title = title;
		root.add(new JLabel(title));
	}

	public JButton addButton(String label, Runnable action) {
		JButton b = new JButton(label);
		b.addActionListener(e -> action.run());
		root.add(b);
		return b;
	}

	@Override
	public JPanel getRoot() {
		return root;
	}

	@Override
	public void onEnter(RouteParams params) {
		// 메뉴는 진입 시 추가 상태가 없다.
	}

	@Override
	public void onExit() {
		// 메뉴는 나갈 때 해제할 자원이 없다.
	}
}
