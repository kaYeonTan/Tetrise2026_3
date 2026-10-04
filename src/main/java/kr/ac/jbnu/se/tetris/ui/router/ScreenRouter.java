package kr.ac.jbnu.se.tetris.ui.router;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

// 2-5절 중앙 라우터 (EDT에서 호출)
public class ScreenRouter {

	JFrame frame;
	JPanel cardHost;
	CardLayout cards;
	JPanel glassPane;
	Map<Route, Supplier<Screen>> factories = new EnumMap<>(Route.class);
	Map<Route, Screen> instances = new EnumMap<>(Route.class);
	List<RouteGuard> guards;
	Deque<Route> screenStack = new ArrayDeque<>();
	Route currentScreen;
	Route topOverlay;

	public ScreenRouter(JFrame frame, List<RouteGuard> guards) {
		this.frame = frame;
		this.guards = guards;
		cards = new CardLayout();
		cardHost = new JPanel(cards);
		frame.setContentPane(cardHost);

		glassPane = new JPanel(new BorderLayout());
		glassPane.setOpaque(false);
		frame.setGlassPane(glassPane);
		glassPane.setVisible(false);
	}

	public void register(Route route, Supplier<Screen> factory) {
		factories.put(route, factory);
	}

	public Screen screen(Route route) {
		return instances.computeIfAbsent(route, r -> {
			Supplier<Screen> f = factories.get(r);
			if (f == null)
				throw new IllegalStateException("no factory: " + r);
			Screen s = f.get();
			if (r.type == RouteType.SCREEN) {
				cardHost.add(s.getRoot(), r.name());
			}
			return s;
		});
	}

	public void navigate(Route to) {
		navigate(to, RouteParams.NONE);
	}

	public void navigate(Route to, RouteParams params) {
		applyGuards(null, to, params, false);
	}

	public void replace(Route to, RouteParams params) {
		applyGuards(currentScreen, to, params, true);
	}

	public void resetTo(Route to) {
		screenStack.clear();
		closeOverlaySilent();
		showScreen(to, RouteParams.NONE, true);
	}

	public void back() {
		if (topOverlay != null) {
			closeOverlay();
			return;
		}
		if (screenStack.isEmpty())
			return;
		Route prev = screenStack.pop();
		showScreen(prev, RouteParams.NONE, true);
	}

	public Route currentScreen() {
		return currentScreen;
	}

	public Optional<Route> topOverlay() {
		return Optional.ofNullable(topOverlay);
	}

	void applyGuards(Route from, Route to, RouteParams params, boolean replace) {
		for (RouteGuard g : guards) {
			GuardResult r = g.check(from != null ? from : currentScreen, to, params, this);
			if (r.decision == GuardResult.Decision.DENY)
				return;
			if (r.decision == GuardResult.Decision.REDIRECT) {
				navigate(r.redirect, r.params);
				return;
			}
		}
		if (to.type == RouteType.OVERLAY)
			openOverlay(to, params);
		else
			showScreen(to, params, replace);
	}

	void showScreen(Route to, RouteParams params, boolean replace) {
		if (currentScreen != null) {
			screen(currentScreen).onExit();
			if (!replace)
				screenStack.push(currentScreen);
		}
		closeOverlaySilent();
		currentScreen = to;
		cards.show(cardHost, to.name());
		screen(to).onEnter(params);
		frame.revalidate();
		frame.repaint();
	}

	void openOverlay(Route overlay, RouteParams params) {
		if (topOverlay != null && topOverlay != overlay)
			screen(topOverlay).onExit();
		topOverlay = overlay;
		Screen s = screen(overlay);
		glassPane.removeAll();
		glassPane.add(s.getRoot(), BorderLayout.CENTER);
		glassPane.setVisible(true);
		s.onEnter(params);
		frame.revalidate();
		frame.repaint();
	}

	void closeOverlay() {
		if (topOverlay == null)
			return;
		screen(topOverlay).onExit();
		closeOverlaySilent();
		frame.revalidate();
		frame.repaint();
	}

	void closeOverlaySilent() {
		if (topOverlay != null)
			screen(topOverlay).onExit();
		topOverlay = null;
		glassPane.removeAll();
		glassPane.setVisible(false);
	}
}
