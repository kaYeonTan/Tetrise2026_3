package kr.ac.jbnu.se.tetris.ui.router;

public interface RouteGuard {
	GuardResult check(Route from, Route to, RouteParams params, ScreenRouter router);
}
