package kr.ac.jbnu.se.tetris.ui.router;

public class LoginParams implements RouteParams {

	public final int player;
	public final Route returnTo;
	public final RouteParams returnParams;

	public LoginParams(int player, Route returnTo, RouteParams returnParams) {
		this.player = player;
		this.returnTo = returnTo;
		this.returnParams = returnParams;
	}

	public LoginParams(Route returnTo, RouteParams returnParams) {
		this(1, returnTo, returnParams);
	}
}
