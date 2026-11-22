package kr.ac.jbnu.se.tetris.ui.router;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScreenRouterTest {

	@Test
	void secondScreenIsShownAndOverlayExitsOnce() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

		JFrame frame = new JFrame();
		try {
			ScreenRouter router = new ScreenRouter(frame, List.of());
			JPanel title = new JPanel();
			title.add(new JLabel("title"));
			JPanel game = new JPanel();
			game.add(new JLabel("game"));
			int[] pauseExits = { 0 };

			router.register(Route.TITLE, () -> fixed(title));
			router.register(Route.GAME, () -> fixed(game));
			router.register(Route.PAUSE, () -> new Screen() {
				private final JPanel root = new JPanel();

				@Override
				public JPanel getRoot() {
					return root;
				}

				@Override
				public void onEnter(RouteParams params) {
				}

				@Override
				public void onExit() {
					pauseExits[0]++;
				}
			});

			router.resetTo(Route.TITLE);
			router.navigate(Route.GAME);
			assertEquals(Route.GAME, router.currentScreen());
			assertTrue(game.isVisible());
			assertFalse(title.isVisible());

			router.navigate(Route.PAUSE);
			router.back();
			assertEquals(1, pauseExits[0]);
			assertEquals(Route.GAME, router.currentScreen());
		} finally {
			frame.dispose();
		}
	}

	private static Screen fixed(JPanel root) {
		return new Screen() {
			@Override
			public JPanel getRoot() {
				return root;
			}

			@Override
			public void onEnter(RouteParams params) {
			}

			@Override
			public void onExit() {
			}
		};
	}
}
