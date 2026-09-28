package kr.ac.jbnu.se.tetris.ui.router;

import javax.swing.JComponent;

public interface Screen {
	JComponent getRoot();

	void onEnter(RouteParams params);

	void onExit();
}
