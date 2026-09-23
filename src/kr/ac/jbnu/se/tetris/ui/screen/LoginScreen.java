package kr.ac.jbnu.se.tetris.ui.screen;

import kr.ac.jbnu.se.tetris.app.AccountService;
import kr.ac.jbnu.se.tetris.ui.router.LoginParams;
import kr.ac.jbnu.se.tetris.ui.router.RouteParams;
import kr.ac.jbnu.se.tetris.ui.router.ScreenRouter;

public class LoginScreen extends SimpleMenuScreen {

	ScreenRouter router;
	AccountService accounts;
	LoginParams pending;

	public LoginScreen(ScreenRouter router, AccountService accounts) {
		super("LOGIN (B팀 UI 자리)");
		this.router = router;
		this.accounts = accounts;
		addButton("로그인 스텁 (testuser)", () -> {
			accounts.loginStub("testuser");
			if (pending != null)
				router.navigate(pending.returnTo, pending.returnParams);
			else
				router.back();
		});
		addButton("뒤로", () -> router.back());
	}

	@Override
	public void onEnter(RouteParams params) {
		pending = params instanceof LoginParams ? (LoginParams) params : null;
	}
}
