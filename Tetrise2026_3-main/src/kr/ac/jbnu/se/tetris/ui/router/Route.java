package kr.ac.jbnu.se.tetris.ui.router;

import java.util.Optional;

// [표 2-5] R-01 ~ R-11
public enum Route {
	TITLE("/title", RouteType.SCREEN),
	LOGIN("/login", RouteType.SCREEN),
	SIGNUP("/signup", RouteType.SCREEN),
	MODE_SELECT("/mode", RouteType.SCREEN),
	COOP_LOBBY("/coop-lobby", RouteType.SCREEN),
	GAME("/game", RouteType.SCREEN),
	PAUSE("/game/pause", RouteType.OVERLAY),
	SAVE_SLOTS("/saves", RouteType.OVERLAY),
	SETTINGS("/settings", RouteType.OVERLAY),
	RESULT("/result", RouteType.SCREEN),
	RECORDS("/records", RouteType.SCREEN);

	public final String path;
	public final RouteType type;

	Route(String path, RouteType type) {
		this.path = path;
		this.type = type;
	}

	public String path() {
		return path;
	}

	public RouteType type() {
		return type;
	}

	public static Optional<Route> fromPath(String path) {
		for (Route r : values()) {
			if (r.path.equals(path))
				return Optional.of(r);
		}
		return Optional.empty();
	}
}
