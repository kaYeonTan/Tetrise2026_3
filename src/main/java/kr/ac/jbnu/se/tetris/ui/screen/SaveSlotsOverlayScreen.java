package kr.ac.jbnu.se.tetris.ui.screen;

import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;

public class SaveSlotsOverlayScreen extends SimpleMenuScreen {

	public SaveSlotsOverlayScreen(ScreenRouter router) {
		super("SAVE SLOTS (F-06 나중에)");
		addButton("닫기", router::back);
	}

}
