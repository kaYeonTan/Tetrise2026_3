package kr.ac.jbnu.se.tetris.ui.router;

public class ParentGuard implements RouteGuard {

	@Override
	public GuardResult check(Route from, Route to, RouteParams params, ScreenRouter router) {
		if (to == Route.PAUSE || to == Route.SETTINGS) {
			if (router.currentScreen() == Route.GAME)
				return GuardResult.allow();
			return GuardResult.deny();
		}
		if (to == Route.SAVE_SLOTS) {
			if (params instanceof SaveSlotParams && ((SaveSlotParams) params).purpose == SavePurpose.LOAD)
				return GuardResult.allow();
			if (router.currentScreen() == Route.GAME)
				return GuardResult.allow();
			return GuardResult.deny();
		}
		return GuardResult.allow();
	}
}
