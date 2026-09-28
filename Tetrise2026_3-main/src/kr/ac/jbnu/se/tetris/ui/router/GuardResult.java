package kr.ac.jbnu.se.tetris.ui.router;

public class GuardResult {

	public enum Decision {
		ALLOW,
		DENY,
		REDIRECT
	}

	public final Decision decision;
	public final Route redirect;
	public final RouteParams params;

	public GuardResult(Decision decision, Route redirect, RouteParams params) {
		this.decision = decision;
		this.redirect = redirect;
		this.params = params;
	}

	public static GuardResult allow() {
		return new GuardResult(Decision.ALLOW, null, RouteParams.NONE);
	}

	public static GuardResult deny() {
		return new GuardResult(Decision.DENY, null, RouteParams.NONE);
	}

	public static GuardResult redirect(Route route, RouteParams params) {
		return new GuardResult(Decision.REDIRECT, route, params != null ? params : RouteParams.NONE);
	}
}
