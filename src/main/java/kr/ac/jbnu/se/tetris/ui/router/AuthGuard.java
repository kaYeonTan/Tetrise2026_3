package kr.ac.jbnu.se.tetris.ui.router;

import kr.ac.jbnu.se.tetris.app.AccountService;

public class AuthGuard implements RouteGuard {

	AccountService accounts;

	public AuthGuard(AccountService accounts) {
		this.accounts = accounts;
	}

	@Override
	public GuardResult check(Route from, Route to, RouteParams params, ScreenRouter router) {
		if (accounts.isLoggedIn())
			return GuardResult.allow();
		if (to == Route.SAVE_SLOTS || to == Route.RECORDS)
			return GuardResult.redirect(Route.LOGIN, new LoginParams(to, params));
		return GuardResult.allow();
	}
}
