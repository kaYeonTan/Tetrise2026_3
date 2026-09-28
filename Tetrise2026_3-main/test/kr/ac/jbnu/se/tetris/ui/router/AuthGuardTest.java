package kr.ac.jbnu.se.tetris.ui.router;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.app.AccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthGuardTest {

	@Test
	void guestSaveSlotsRedirectsToLogin() {
		AccountService accounts = new AccountService();
		AuthGuard guard = new AuthGuard(accounts);
		GuardResult r = guard.check(Route.GAME, Route.SAVE_SLOTS, new SaveSlotParams(SavePurpose.SAVE), null);
		assertEquals(GuardResult.Decision.REDIRECT, r.decision);
		assertEquals(Route.LOGIN, r.redirect);
	}

	@Test
	void loggedInAllowsSaveSlots() {
		AccountService accounts = new AccountService();
		accounts.loginStub("u");
		AuthGuard guard = new AuthGuard(accounts);
		GuardResult r = guard.check(Route.GAME, Route.SAVE_SLOTS, new SaveSlotParams(SavePurpose.LOAD), null);
		assertEquals(GuardResult.Decision.ALLOW, r.decision);
	}
}
