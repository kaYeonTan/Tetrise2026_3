package kr.ac.jbnu.se.tetris.ui.router;

import javax.swing.JOptionPane;

public class LeaveGuard implements RouteGuard {

	public interface ConfirmDialog {
		boolean confirmLeave();
	}

	ConfirmDialog dialog = () -> JOptionPane.showConfirmDialog(null,
			"저장하지 않은 진행 상황은 사라집니다. 나가시겠습니까?",
			"나가기",
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

	public void setDialog(ConfirmDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public GuardResult check(Route from, Route to, RouteParams params, ScreenRouter router) {
		if (to != Route.TITLE)
			return GuardResult.allow();
		if (router.currentScreen() != Route.GAME)
			return GuardResult.allow();
		if (!dialog.confirmLeave())
			return GuardResult.deny();
		return GuardResult.allow();
	}
}
