package kr.ac.jbnu.se.tetris.app;

import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import kr.ac.jbnu.se.tetris.core.GameMode;
import kr.ac.jbnu.se.tetris.ui.router.AuthGuard;
import kr.ac.jbnu.se.tetris.ui.router.LeaveGuard;
import kr.ac.jbnu.se.tetris.ui.router.ParentGuard;
import kr.ac.jbnu.se.tetris.ui.router.Route;
import kr.ac.jbnu.se.tetris.ui.router.SavePurpose;
import kr.ac.jbnu.se.tetris.ui.router.SaveSlotParams;
import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;
import kr.ac.jbnu.se.tetris.ui.screen.GameScreen;
import kr.ac.jbnu.se.tetris.ui.screen.LoginScreen;
import kr.ac.jbnu.se.tetris.ui.screen.PauseOverlayScreen;
import kr.ac.jbnu.se.tetris.ui.screen.SaveSlotsOverlayScreen;
import kr.ac.jbnu.se.tetris.ui.screen.SimpleMenuScreen;
import kr.ac.jbnu.se.tetris.ui.router.GameParams;

public class GameWindow {

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(GameWindow::createAndShow);
	}

	static void createAndShow() {
		AccountService accounts = new AccountService();
		JFrame frame = new JFrame("Tetris");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(360, 440);

		ScreenRouter router = new ScreenRouter(frame,
				Arrays.asList(new AuthGuard(accounts), new LeaveGuard(), new ParentGuard()));

		GameScreen gameScreen = new GameScreen(router);

		router.register(Route.TITLE, () -> {
			SimpleMenuScreen s = new SimpleMenuScreen("TETRIS 2026");
			s.addButton("Start", () -> router.navigate(Route.MODE_SELECT));
			s.addButton("Load (게스트→LOGIN)", () -> router.navigate(Route.SAVE_SLOTS,
					new SaveSlotParams(SavePurpose.LOAD)));
			return s;
		});
		router.register(Route.MODE_SELECT, () -> {
			SimpleMenuScreen s = new SimpleMenuScreen("MODE");
			s.addButton("1 Player", () -> router.navigate(Route.GAME, new GameParams(GameMode.SINGLE)));
			s.addButton("Back", () -> router.back());
			return s;
		});
		router.register(Route.GAME, () -> gameScreen);
		router.register(Route.PAUSE, () -> new PauseOverlayScreen(router, gameScreen));
		router.register(Route.SAVE_SLOTS, () -> new SaveSlotsOverlayScreen(router));
		router.register(Route.LOGIN, () -> new LoginScreen(router, accounts));
		router.register(Route.SIGNUP, () -> new SimpleMenuScreen("SIGNUP (B)"));
		router.register(Route.COOP_LOBBY, () -> new SimpleMenuScreen("COOP LOBBY (B)"));
		router.register(Route.SETTINGS, () -> {
			SimpleMenuScreen s = new SimpleMenuScreen("SETTINGS");
			s.addButton("Close", router::back);
			return s;
		});
		router.register(Route.RESULT, () -> new SimpleMenuScreen("RESULT"));
		router.register(Route.RECORDS, () -> {
			SimpleMenuScreen s = new SimpleMenuScreen("RECORDS");
			s.addButton("Back", () -> router.navigate(Route.TITLE));
			return s;
		});

		router.resetTo(Route.TITLE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
